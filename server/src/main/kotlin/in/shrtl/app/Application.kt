@file:Suppress(
    "ktlint:standard:no-wildcard-imports",
    "ktlint:standard:package-name",
    "NestedLambdaShadowedImplicitParameter",
)

package `in`.shrtl.app

import AuthResult
import Challenge
import DIFFICULTY_PREFIX
import EndpointWithArg
import GetClicksRequest
import GetUrlsRequest
import Greeting
import IS_LOCALHOST
import Period
import ProofOfWork
import RemoveUrlRequest
import SERVER_PORT
import SQUARE_ICON_DATA
import UpdateNickRequest
import UrlInfo
import UrlStats
import UrlsResponse
import User
import challengeHash
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.engine.*
import io.ktor.server.http.content.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.*
import io.ktor.util.collections.*
import kotlinx.datetime.*
import kotlinx.datetime.TimeZone
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import redis.clients.jedis.JedisPool
import redis.clients.jedis.JedisPoolConfig
import redis.clients.jedis.timeseries.TimeSeriesProtocol.TimeSeriesCommand
import java.io.File
import java.security.KeyFactory
import java.security.SecureRandom
import java.security.interfaces.RSAPrivateCrtKey
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.RSAPublicKeySpec
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*
import kotlin.math.ceil
import kotlin.text.toCharArray

fun main() {
    initDB()
    initRedis()
    println("Starting server at $HOST_LOCAL:$SERVER_PORT")
    embeddedServer(Netty, port = SERVER_PORT, host = HOST_LOCAL, module = Application::module)
        .start(wait = true)
}

private const val CHALLENGE_EXPIRATION_TIME_MS = 60_000 // 1 minute in milliseconds
private const val HOST_LOCAL = "0.0.0.0"
private const val DEFAULT_DB_PASSWORD = "password"
const val CLAIM_USER_ID = "uid"
private val hostRelease =
    System.getenv("DOMAIN").takeIf { !it.isNullOrBlank() }.also {
        println("DOMAIN from env: $it")
    } ?: "shrtl.in"
private val hostName = if (IS_LOCALHOST) HOST_LOCAL else hostRelease
private val hostUrl = if (IS_LOCALHOST) "http://$hostName:$SERVER_PORT" else "https://$hostName"
private val privateKeyPath = if (IS_LOCALHOST) "./server/ktor.pk8" else "/run/secrets/ktor_pk8"
private val certsPath = if (IS_LOCALHOST) "./server/certs" else "/run/secrets/certs"
private val SQUARE_ICON_BYTES = SQUARE_ICON_DATA.decodeBase64Bytes()
private val redishost = if (IS_LOCALHOST) "localhost" else "redis"
private val redisPort = 6379
lateinit var jedisPool: JedisPool

object Urls : LongIdTable() {
    val originalUrl = varchar("original_url", 2048)
    val shortUrl = varchar("short_url", 255)
    val comment = varchar("comment", 2048)
    val userId = long("user_id").references(Users.id)
    val timestamp = long("timestamp")
    val clicks = long("clicks")
    val qrClicks = long("qr_clicks")
}

object Users : LongIdTable() {
    val nick = varchar("nick", 255)
    val timestamp = long("timestamp")
    val challenge = varchar("challenge", 2048).uniqueIndex()
}

fun initDB() {
    Database.connect(
        url = getDatabaseUrl().also { println("DATABASE_URL: $it") },
        driver = "org.postgresql.Driver",
        user = getDatabaseUser().also { println("DATABASE_USER: $it") },
        password = getDatabasePassword().also { println("DATABASE_PASSWORD is '$DEFAULT_DB_PASSWORD'?: ${it == DEFAULT_DB_PASSWORD}") },
    )
    transaction {
        SchemaUtils.create(Urls, Users)
        // Check and add the clicks column if it doesn't exist
        val clicksColumnExists =
            exec(
                "SELECT column_name FROM information_schema.columns WHERE table_name='urls' AND column_name='clicks'",
            ) { rs -> rs.next() }

        if (clicksColumnExists == false) {
            exec("ALTER TABLE urls ADD COLUMN clicks BIGINT DEFAULT 0")
        }

        // Check and add the qr_clicks column if it doesn't exist
        val qrClicksColumnExists =
            exec(
                "SELECT column_name FROM information_schema.columns WHERE table_name='urls' AND column_name='qr_clicks'",
            ) { rs -> rs.next() }

        if (qrClicksColumnExists == false) {
            exec("ALTER TABLE urls ADD COLUMN qr_clicks BIGINT DEFAULT 0")
        }
    }
}

fun initRedis() {
    val poolConfig = JedisPoolConfig()
    jedisPool = JedisPool(poolConfig, redishost, redisPort)
}

private fun getDatabasePassword() =
    if (IS_LOCALHOST) {
        DEFAULT_DB_PASSWORD
    } else {
        System
            .getenv("DATABASE_PASSWORD_FILE")
            .takeIf { !it.isNullOrBlank() }
            ?.let { File(it).readText().trim() }
            .takeIf { !it.isNullOrBlank() }
            .also { println("DATABASE_PASSWORD from file isNullOrEmpty?: ${it.isNullOrEmpty()}") }
            ?: DEFAULT_DB_PASSWORD
    }

private fun getDatabaseUser() =
    if (IS_LOCALHOST) {
        "user"
    } else {
        System
            .getenv("DATABASE_USER_FILE")
            .takeIf { !it.isNullOrBlank() }
            ?.let { File(it).readText().trim() }
            .takeIf { !it.isNullOrBlank() }
            .also { println("DATABASE_USER from file: $it") } ?: "user"
    }

private fun getDatabaseUrl() =
    if (IS_LOCALHOST) {
        "jdbc:postgresql://0.0.0.0:5432/shrtlin"
    } else {
        System.getenv("DATABASE_URL").takeIf { !it.isNullOrBlank() }.also { println("DATABASE_URL from env: $it") }
            ?: "jdbc:postgresql://postgres:5432/shrtlin"
    }

// https://github.com/ktorio/ktor-documentation/blob/2.3.10/codeSnippets/snippets/auth-jwt-rs256/src/main/kotlin/com/example/Application.kt
fun Application.module() {
    install(CORS) {
        anyHost()
        allowCredentials = true
        allowNonSimpleContentTypes = true
        allowSameOrigin = true
        allowHeader(HttpHeaders.Authorization)
        allowHeader(HttpHeaders.ContentType)
        allowHeader(HttpHeaders.AccessControlAllowOrigin)
    }
    install(ContentNegotiation) {
        json()
    }
    val issuer = hostUrl
    val audience = "in.shrtl.app"
    val myRealm = "in.shrtl.app"
    val jwtAlgorithm = loadJWTKey()
    val jwtVerifier = JWT.require(jwtAlgorithm).withIssuer(issuer).build()

    install(Authentication) {
        jwt("auth-jwt") {
            realm = myRealm
            verifier(jwtVerifier)
            validate { credential ->
                if (credential.payload.audience.contains(audience)) {
                    JWTPrincipal(credential.payload)
                } else {
                    null
                }
            }
            challenge { _, _ ->
                call.respond(HttpStatusCode.Unauthorized, "Token is not valid or expired")
            }
        }
    }
    routing {
        get("/") { call.respondText("Ktor: ${Greeting().greet()}") }
        get("/favicon.ico") {
            println("Favicon requested")
            call.respondBytes(SQUARE_ICON_BYTES, ContentType.Image.SVG)
        }
        File(certsPath).walkTopDown().forEach { println(it) }
        File(certsPath, "jwks.json").readText().also { println(it) }
        static(".well-known") {
            staticRootFolder = File(certsPath)
            static("jwks.json") {
                files("jwks.json")
            }
        }
        get("/debug/jwks") {
            val jwksFile = File(certsPath, "jwks.json")
            if (jwksFile.exists()) {
                println("Serving JWKS")
                call.respondText(jwksFile.readText())
            } else {
                println("JWKS not found")
                call.respondText("File not found", status = HttpStatusCode.NotFound)
            }
        }
        get(Endpoints.powGet.path) {
            call.respond(issueChallenge(jwtAlgorithm))
        }
        post(Endpoints.powPost()) {
            var respondError = suspend {}
            receiveArgs(it) { proofOfWork ->
                if (validateChallengeSolution(proofOfWork, jwtAlgorithm)) {
                    val createdUser = addUser("user", proofOfWork.challenge)
                    if (createdUser != null) {
                        // create JWT refreshToken, infinity lifetime
                        val refreshToken =
                            JWT
                                .create()
                                .withAudience(audience)
                                .withIssuer(issuer)
                                .withClaim(CLAIM_USER_ID, createdUser.id)
                                .sign(jwtAlgorithm)
                        // create JWT sessionToken
                        val sessionToken =
                            JWT
                                .create()
                                .withAudience(audience)
                                .withIssuer(issuer)
                                .withExpiresAt(
                                    Instant
                                        .now()
                                        .plus(1L, ChronoUnit.DAYS)
                                        .plus((0..1000).random().toLong(), ChronoUnit.MILLIS),
                                ).withClaim(CLAIM_USER_ID, createdUser.id)
                                .sign(jwtAlgorithm)
                        AuthResult(refreshToken, sessionToken, createdUser)
                    } else {
                        respondError = { call.respond(HttpStatusCode.Conflict, "User already exists") }
                        null
                    }
                } else {
                    respondError = { call.respond(HttpStatusCode.BadRequest, "Invalid Proof of Work Solution") }
                    null
                }
            }?.let { authResult -> call.respond(HttpStatusCode.Created, authResult) } ?: respondError()
        }
        post(Endpoints.tokenRefresh()) {
            receiveArgs(it) { refreshToken ->
                val jwt = jwtVerifier.verify(refreshToken.refreshToken)
                val userId = jwt.getClaim(CLAIM_USER_ID).asLong()
                val user = getUser(userId)
                if (user != null) {
                    val sessionToken =
                        JWT
                            .create()
                            .withAudience(audience)
                            .withIssuer(issuer)
                            .withClaim(CLAIM_USER_ID, user.id)
                            .sign(jwtAlgorithm)
                    AuthResult(refreshToken.refreshToken, sessionToken, user)
                } else {
                    null
                }
            }?.let { call.respond(HttpStatusCode.Created, it) } ?: call.respond(
                HttpStatusCode.NotFound,
                "User Not Found",
            )
        }

        authenticate("auth-jwt") {
            post(Endpoints.shorten()) {
                val userId =
                    call
                        .principal<JWTPrincipal>()
                        ?.payload
                        ?.getClaim(CLAIM_USER_ID)
                        ?.asLong()
                if (userId == null) {
                    call.respond(HttpStatusCode.Unauthorized, "User Not Found or invalid token")
                    return@post
                }
                receiveArgs(it) { shortenRequest ->
                    val orig =
                        shortenRequest.url
                            .trim()
                            .removePrefix("http://")
                            .removePrefix("https://")
                    transaction {
                        val urlId =
                            Urls.insert {
                                it[originalUrl] = orig
                                it[shortUrl] = orig
                                it[comment] = ""
                                it[Urls.userId] = userId
                                it[timestamp] = Clock.System.now().toEpochMilliseconds() - (0..1000).random()
                                it[clicks] = 0
                                it[qrClicks] = 0
                            } get Urls.id

                        val chars = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz".toCharArray()
                        val base = chars.size.toLong()

                        fun encodeUrlId(id: Long) =
                            buildString {
                                var num = id
                                while (num > 0) {
                                    append(chars[(num % base).toInt()])
                                    num /= base
                                }
                            }

                        val newUrl = encodeUrlId(urlId.value)
                        Urls.update({ Urls.id eq urlId }) {
                            it[shortUrl] = newUrl
                        }
                        println("Shortening: ${shortenRequest.url} -> $newUrl")
                        val timestamp = Clock.System.now().toEpochMilliseconds() + (0..1000).random()
                        UrlInfo(
                            urlId.value,
                            shortenRequest.url,
                            "$hostUrl/$newUrl", // TODO: maybe not include the host
                            "",
                            userId,
                            timestamp,
                            0,
                            0,
                        )
                    }
                }?.let { call.respond(HttpStatusCode.Created, it) } ?: call.respond(
                    HttpStatusCode.BadRequest,
                    "Invalid URL",
                )
            }
            post(Endpoints.getUrls()) {
                val userId =
                    call
                        .principal<JWTPrincipal>()
                        ?.payload
                        ?.getClaim(CLAIM_USER_ID)
                        ?.asLong()
                if (userId == null) {
                    call.respond(HttpStatusCode.Unauthorized, "User Not Found or invalid token")
                    return@post
                }
                receiveArgs(it) { req: GetUrlsRequest ->
                    val offset = (req.page - 1).toLong() * req.pageSize.toLong()
                    val totalCount = transaction { Urls.select { Urls.userId eq userId }.count() }
                    val urls =
                        transaction {
                            Urls
                                .select { Urls.userId eq userId }
                                .limit(req.pageSize, offset)
                                .map {
                                    UrlInfo(
                                        it[Urls.id].value,
                                        it[Urls.originalUrl],
                                        "$hostUrl/${it[Urls.shortUrl]}",
                                        it[Urls.comment],
                                        it[Urls.userId],
                                        it[Urls.timestamp],
                                        it[Urls.clicks],
                                        it[Urls.qrClicks],
                                    )
                                }.toList()
                        }
                    UrlsResponse(urls, ceil(totalCount / req.pageSize.toDouble()).toInt())
                }?.let { call.respond(it) } ?: call.respond(HttpStatusCode.BadRequest, "Invalid pagination parameters")
            }
            post(Endpoints.removeUrl()) {
                val userId =
                    call
                        .principal<JWTPrincipal>()
                        ?.payload
                        ?.getClaim(CLAIM_USER_ID)
                        ?.asLong()
                if (userId == null) {
                    call.respond(HttpStatusCode.Unauthorized, "User Not Found or invalid token")
                    return@post
                }
                receiveArgs(it) { request: RemoveUrlRequest ->
                    transaction {
                        val deletedCount =
                            Urls.deleteWhere {
                                (Urls.id eq request.id) and (Urls.userId eq userId)
                            }
                        deletedCount > 0 // Return true if deletion was successful
                    }
                }?.let { success -> call.respond(HttpStatusCode.OK, success) }
                    ?: call.respond(HttpStatusCode.BadRequest, "Invalid request")
            }
            postWithAuth(Endpoints.updateNick()) { (endpoint, userId) ->
                receiveArgs(endpoint) { request: UpdateNickRequest ->
                    transaction {
                        Users.update({ Users.id eq userId }) {
                            it[nick] = request.nick
                        } > 0
                    }
                }?.let { success -> call.respond(HttpStatusCode.OK, success) }
                    ?: call.respond(HttpStatusCode.BadRequest, "Invalid request")
            }
            post(Endpoints.getClicks()) {
                val userId =
                    call
                        .principal<JWTPrincipal>()
                        ?.payload
                        ?.getClaim(CLAIM_USER_ID)
                        ?.asLong()
                if (userId == null) {
                    call.respond(HttpStatusCode.Unauthorized, "User Not Found or invalid token")
                    return@post
                }
                receiveArgs(it) { request: GetClicksRequest ->
                    val clicks = getClicksFromRedisTimeSeries(request.urlId, request.period)
                    UrlStats(clicks = clicks)
                }?.let { result -> call.respond(HttpStatusCode.OK, result) }
                    ?: call.respond(HttpStatusCode.BadRequest, "Invalid request")
            }
        }
        get("/{shortUrl}") {
            val shortUrl = call.parameters["shortUrl"]
            println("Redirecting: $shortUrl")
            val urlInfo =
                transaction {
                    val info =
                        Urls.select { Urls.shortUrl eq shortUrl!! }.singleOrNull()?.let {
                            UrlInfo(
                                it[Urls.id].value,
                                it[Urls.originalUrl],
                                "$hostUrl/${it[Urls.shortUrl]}",
                                it[Urls.comment],
                                it[Urls.userId],
                                it[Urls.timestamp],
                                it[Urls.clicks],
                                it[Urls.qrClicks],
                            )
                        }
                    if (info != null) {
                        Urls.update({ Urls.id eq info.id }) {
                            it[clicks] = 1 + info.clicks
                        }
                    }
                    info
                }

            println("Original URL: ${urlInfo?.originalUrl}")
            if (urlInfo != null) {
                recordClick(urlInfo.id)
                call.respondRedirect("http://${urlInfo.originalUrl}")
            } else {
                call.respond(HttpStatusCode.NotFound, "URL Not Found")
            }
        }
        get("/{shortUrl}/qr/") {
            val shortUrl = call.parameters["shortUrl"]
            println("Redirecting: $shortUrl")
            val urlInfo =
                transaction {
                    val info =
                        Urls.select { Urls.shortUrl eq shortUrl!! }.singleOrNull()?.let {
                            UrlInfo(
                                it[Urls.id].value,
                                it[Urls.originalUrl],
                                "$hostUrl/${it[Urls.shortUrl]}",
                                it[Urls.comment],
                                it[Urls.userId],
                                it[Urls.timestamp],
                                it[Urls.clicks],
                                it[Urls.qrClicks],
                            )
                        }
                    if (info != null) {
                        Urls.update({ Urls.id eq info.id }) {
                            it[qrClicks] = 1 + info.qrClicks
                        }
                    }
                    info
                }

            println("Original URL: ${urlInfo?.originalUrl}")
            if (urlInfo != null) {
                recordClick(urlInfo.id)
                call.respondRedirect("http://${urlInfo.originalUrl}")
            } else {
                call.respond(HttpStatusCode.NotFound, "URL Not Found")
            }
        }
    }
}

fun getClicksFromRedisTimeSeries(
    urlId: Long,
    period: Period,
): Map<String, Int> {
    val jedis = jedisPool.resource
    val key = "clicks:url:$urlId"
    val aggregationType = "COUNT"
    val bucket = period.timeBucketSeconds

    val now = Clock.System.now()
    val endTime = now.toEpochMilliseconds()
    val startTime =
        when (period) {
            Period.MINUTE -> now.minus(1, DateTimeUnit.MINUTE).toEpochMilliseconds()
            Period.HOUR -> now.minus(1, DateTimeUnit.HOUR).toEpochMilliseconds()
            Period.DAY -> now.minus(1, DateTimeUnit.DAY, TimeZone.UTC).toEpochMilliseconds()
            Period.MONTH -> now.minus(1, DateTimeUnit.MONTH, TimeZone.UTC).toEpochMilliseconds()
            Period.YEAR -> now.minus(1, DateTimeUnit.YEAR, TimeZone.UTC).toEpochMilliseconds()
        }

    return try {
        val response =
            jedis.sendCommand(
                TimeSeriesCommand.RANGE,
                key,
                startTime.toString(),
                endTime.toString(),
                "AGGREGATION",
                aggregationType,
                bucket.toString(),
            )

        // Convert the response to a more usable format
        val result =
            (response as List<List<Any>>).associate { dataPoint ->
                val rawTimestamp = dataPoint[0] as Long
                val count = String(dataPoint[1] as ByteArray).toLong().toInt()
                val dateTime =
                    kotlinx.datetime.Instant
                        .fromEpochMilliseconds(rawTimestamp)
                        .toLocalDateTime(TimeZone.UTC)
                val formatter = SimpleDateFormat(period.dateFormat, Locale.US)
                rawTimestamp.toString() to count
            }

        result
    } catch (e: Exception) {
        println("Error while fetching data from Redis: ${e.message}")
        e.printStackTrace()
        emptyMap()
    } finally {
        jedis.close()
    }
}

fun recordClick(urlId: Long) {
    val jedis = jedisPool.resource
    val key = "clicks:url:$urlId"
    try {
        jedis.sendCommand(TimeSeriesCommand.ADD, key, "*", "1")
    } catch (e: Exception) {
        println("Error while recording click in Redis: ${e.message}")
        e.printStackTrace()
    } finally {
        jedis.close()
    }
}

private fun loadJWTKey(): Algorithm {
    val keyBytes =
        File(privateKeyPath).takeIf { it.exists() }?.readBytes() ?: run {
            // print all files tree
            File(".").walkTopDown().forEach { println(it) }
            throw Exception("Private key file not found")
        }
    val kf = KeyFactory.getInstance("RSA")
    val privateKey = kf.generatePrivate(PKCS8EncodedKeySpec(keyBytes)) as RSAPrivateCrtKey
    val rsaPublicKey =
        kf.generatePublic(RSAPublicKeySpec(privateKey.modulus, privateKey.publicExponent)) as RSAPublicKey
    val rsaPrivateKey = privateKey as RSAPrivateKey
    return Algorithm.RSA512(
        rsaPublicKey,
        rsaPrivateKey,
    )
}

fun issueChallenge(algorithm: Algorithm): Challenge {
    val bytes = ByteArray((8..32).random())
    SecureRandom().nextBytes(bytes)
    val randomHex = bytes.joinToString("") { "%02x".format(it) }
    val currentTimestamp = Clock.System.now().toEpochMilliseconds() - (0..1000).random()
    return Challenge(
        JWT
            .create()
            .withExpiresAt(Instant.ofEpochMilli(currentTimestamp + CHALLENGE_EXPIRATION_TIME_MS))
            .withIssuer("in.shrtl.app")
            .withClaim("nonce", "$randomHex.$currentTimestamp")
            .sign(algorithm),
    )
}

fun validateChallengeSolution(
    pow: ProofOfWork,
    algorithm: Algorithm?,
) = validateChallengeSignatureAndTs(pow.challenge, algorithm) &&
    validateSolution(pow)

fun validateChallengeSignatureAndTs(
    challenge: String,
    algorithm: Algorithm?,
): Boolean {
    val jwt =
        try {
            JWT
                .require(algorithm)
                .withIssuer("in.shrtl.app")
                .build()
                .verify(challenge)
        } catch (e: Exception) {
            return false
        }

    val expiresDateInstant = jwt.expiresAtAsInstant ?: return false
    val currentTimestamp = Clock.System.now().toEpochMilliseconds() - (0..1000).random()
    return expiresDateInstant.toEpochMilli() >= currentTimestamp
}

fun validateSolution(pow: ProofOfWork) =
    pow.prefix == DIFFICULTY_PREFIX &&
        pow.solution.startsWith(pow.challenge) &&
        challengeHash(pow.solution).startsWith(pow.prefix)

/*
+----------------------+                             +----------------------+
|``````````````````````|                             |``````````````````````|
|      Client          |                             |      Server          |
|``````````````````````|                             |``````````````````````|
+---------+------------+                             +------------+---------+
          |     ┌─────────────────┐                               │
          |     │ Request PoW     │                               │
          |     │ Challenge       │                               │
          +---->└─────────────────┘------------------------------>│
          |                                                       │
          |                                        ┌─────────────────────────────┐                   │
          |◄───────────────────────────────────────│ Generate PoW Challenge JWT  │
          |                                        │ with timestamp & challenge  │
          |                                        └─────────────────────────────┘
          |     ┌─────────────────┐                               │
          |     │ Solve PoW       │                               │
          |     │ Challenge       │                               │
          |     └─────────────────┘                               │
          |     ┌─────────────────┐                               │
          +-----│ Submit PoW      │------------------------------>│
          |     │                 │                        ┌──────────────┐
          |     └─────────────────┘                        │ Validate     │
          |                                                │ Challenge    │
          | ┌─────────────────────────────┐                │ JWT, Create  │
          | │ Store Refresh JWT in secure │◄───────────────│ User, Issue  │
          | │ device storage              │                │ Refresh JWT  │
          | └─────────────────────────────┘                └──────────────┘
          │     ┌─────────────────────────────┐                   │
          │     │ Daily: Request Session JWT  │            ┌──────────────┐
          +---->│ using Refresh JWT           │----------->│ Validate     │
          │     │                             │            │ Refresh JWT, │
          │     └─────────────────────────────┘            │ Issue        │
          │                                                │ Session JWT  │
          │                                                └──────────────┘
          │     ┌─────────────────────────────┐                   │
          │     │ Use Session JWT for API     │◄──────────────────┘
          │     │ access                      │
          │     └─────────────────────────────┘
+---------+------------+                              +------------+---------+
      ● Client Side ●                                      ● Server Side ●

*/
val usedUsersChallenges = ConcurrentSet<String>()

fun addUser(
    nick: String,
    challenge: String,
) = // check in memory if a user with the same challenge exists
    if (!usedUsersChallenges.add(challenge)) {
        null
    } else {
        if (usedUsersChallenges.size > 1000) {
            usedUsersChallenges.clear()
        }
        transaction {
            // check if user with the same challenge exists
            val existingUser =
                Users
                    .select { Users.challenge eq challenge }
                    .map {
                        User(it[Users.id].value, it[Users.nick])
                    }.singleOrNull()
            if (existingUser != null) {
                // user with the same challenge exists, don't create a new one
                return@transaction null
            }
            val userId =
                Users.insertAndGetId {
                    it[Users.nick] = nick
                    it[Users.challenge] = challenge
                    it[timestamp] = Clock.System.now().toEpochMilliseconds() - (0..1000).random()
                }
            User(userId.value, nick)
        }
    }

fun getUser(id: Long) =
    transaction {
        Users
            .select { Users.id eq id }
            .map {
                User(it[Users.id].value, it[Users.nick])
            }.singleOrNull()
    }

@Suppress("UNUSED_PARAMETER")
suspend inline fun <reified A : Any, reified R : Any> RoutingContext.receiveArgs(
    e: EndpointWithArg<A, R>,
    makeResult: RoutingContext.(A) -> R?,
): R? = makeResult(call.receive<A>())

inline fun <reified A : Any, reified R : Any> Route.post(
    endpoint: EndpointWithArg<A, R>,
    crossinline body: suspend RoutingContext.(EndpointWithArg<A, R>) -> Unit,
): Route = post(endpoint.path) { body(endpoint) }

inline fun <reified A : Any, reified R : Any> Route.postWithAuth(
    endpoint: EndpointWithArg<A, R>,
    crossinline body: suspend RoutingContext.(Pair<EndpointWithArg<A, R>, Long>) -> Unit,
): Route =
    post(endpoint.path) {
        val userId =
            call
                .principal<JWTPrincipal>()
                ?.payload
                ?.getClaim(CLAIM_USER_ID)
                ?.asLong()
        if (userId == null) {
            call.respond(HttpStatusCode.Unauthorized, "User Not Found or invalid token")
            return@post
        }
        body(endpoint to userId)
    }
