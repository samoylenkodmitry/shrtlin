Directory tree with max depth of 3:
```
.
├── all.md
├── build.gradle.kts
├── composeApp
│   ├── build.gradle.kts
│   ├── Dockerfile
│   └── src
│       ├── androidMain
│       │   ├── AndroidManifest.xml
│       │   ├── kotlin
│       │   │   └── in
│       │   └── res
│       │       ├── drawable
│       │       ├── drawable-v24
│       │       ├── mipmap-anydpi-v26
│       │       ├── mipmap-hdpi
│       │       ├── mipmap-mdpi
│       │       ├── mipmap-xhdpi
│       │       ├── mipmap-xxhdpi
│       │       ├── mipmap-xxxhdpi
│       │       └── values
│       ├── appleMain
│       │   └── kotlin
│       ├── commonMain
│       │   ├── composeResources
│       │   │   └── drawable
│       │   └── kotlin
│       │       ├── Api.kt
│       │       ├── ComposeApp.kt
│       │       └── Storage.kt
│       ├── desktopMain
│       │   └── kotlin
│       │       └── main.kt
│       ├── iosMain
│       │   └── kotlin
│       │       └── MainViewController.kt
│       └── wasmJsMain
│           ├── kotlin
│           │   └── main.kt
│           └── resources
│               └── index.html
├── deploy.sh
├── docker-compose.yml
├── gradle
│   ├── libs.versions.toml
│   └── wrapper
│       ├── gradle-wrapper.jar
│       └── gradle-wrapper.properties
├── gradle.properties
├── gradlew
├── gradlew.bat
├── iosApp
│   ├── Configuration
│   │   └── Config.xcconfig
│   ├── iosApp
│   │   ├── Assets.xcassets
│   │   │   ├── AccentColor.colorset
│   │   │   │   └── Contents.json
│   │   │   ├── AppIcon.appiconset
│   │   │   │   ├── app-icon-1024.png
│   │   │   │   └── Contents.json
│   │   │   └── Contents.json
│   │   ├── ContentView.swift
│   │   ├── Info.plist
│   │   ├── iOSApp.swift
│   │   └── Preview Content
│   │       └── Preview Assets.xcassets
│   │           └── Contents.json
│   └── iosApp.xcodeproj
│       └── project.pbxproj
├── kotlin-js-store
├── LICENSE
├── local.properties
├── README.md
├── runweb.sh
├── sd.excalidraw
├── sd.png
├── server
│   ├── build.gradle.kts
│   ├── certs
│   │   └── jwks.json
│   ├── docker-compose.yml
│   ├── Dockerfile
│   ├── gen_jwks_from_key.sh
│   ├── gen_new_rss_key.sh
│   ├── ktor.pk8
│   └── src
│       └── main
│           ├── kotlin
│           │   └── in
│           └── resources
│               └── logback.xml
├── settings.gradle.kts
├── shared
│   ├── build.gradle.kts
│   └── src
│       ├── androidMain
│       │   └── kotlin
│       │       ├── Platform.android.kt
│       │       └── UrlOpener.android.kt
│       ├── appleMain
│       │   └── kotlin
│       ├── commonMain
│       │   └── kotlin
│       │       ├── Challenge.kt
│       │       ├── Constants.kt
│       │       ├── Data.kt
│       │       ├── Greeting.kt
│       │       ├── Platform.kt
│       │       └── UrlOpener.kt
│       ├── iosMain
│       │   └── kotlin
│       │       ├── Platform.ios.kt
│       │       └── UrlOpener.ios.kt
│       ├── jvmMain
│       │   └── kotlin
│       │       ├── Platform.jvm.kt
│       │       └── UrlOpener.jvm.kt
│       └── wasmJsMain
│           └── kotlin
│               ├── Platform.wasmJs.kt
│               └── UrlOpener.wasmJs.kt
├── template.env
└── totext.sh

62 directories, 62 files
```



#### File: `./deploy.sh`
```


#!/bin/bash

# --- Build Applications --- 

# Build frontend (Web)
./gradlew :composeApp:build

# Build backend
./gradlew :server:build

# --- Generate RSA Keys if not present ---

if [ ! -f "./server/ktor.pk8" ]; then
  echo "RSA keys not found, generating..."
  ./server/gen_new_rss_key.sh
  ./server/gen_jwks_from_key.sh 
  # Securely store ktor.pk8 - adjust permissions
  chmod 400 server/ktor.pk8 
  chmod 400 server/ktor.key
else 
  echo "RSA keys found, skipping generation..."
fi

# --- Configure Environment (.env) ---

if [ ! -f "./.env" ]; then
  echo "Copying template.env to .env"
  cp ./template.env ./.env

  read -p "Enter your database username (default: user): " DB_USER
  DB_USER=${DB_USER:-user} # Use 'user' as default if empty
  sed -i "s/DATABASE_USER=.*/DATABASE_USER=$DB_USER/" ./.env

  read -s -p "Enter your database password (default: password): " DB_PASSWORD
  DB_PASSWORD=${DB_PASSWORD:-password} # Use 'password' as default if empty
  sed -i "s/DATABASE_PASSWORD=.*/DATABASE_PASSWORD=$DB_PASSWORD/" ./.env

  echo "Database credentials updated in .env"
fi
export $(grep -v '^#' .env | xargs) 

# --- Create Docker Secrets (only on first run) --- 

if [ ! "$(docker secret ls -q | grep db_username)" ]; then 
  echo "Creating Docker secrets..."
  docker secret create db_username "$DATABASE_USER"
  docker secret create db_password "$DATABASE_PASSWORD"
else 
  echo "Docker secrets already exist, skipping creation."
fi

# --- Start Docker Compose ---
docker-compose up -d

echo "Deployment complete!"

```



#### File: `./README.md`
```


# shrtlin - A Kotlin Multiplatform URL Shortener
`shrtlin` is a feature-rich, open-source URL shortening service built with Kotlin Multiplatform.
It provides a seamless experience across Android, iOS, Web, and Desktop, powered by a Ktor backend and PostgreSQL database.

## About

shrtlin leverages the power of Kotlin Multiplatform to deliver a unified codebase for multiple platforms. It offers a user-friendly interface for shortening URLs and managing your links, all while ensuring secure authentication using JWT tokens and Proof of Work.

## Features

* **Easy URL shortening:** Quickly shorten URLs using intuitive interfaces on Android, iOS, Web, and Desktop.
* **Seamless Authentication:** Securely authenticate using JWT tokens and Proof of Work.
* **URL Management:** View, edit, and delete your shortened URLs.
* **Cross-platform compatibility:** Enjoy a consistent experience across Android, iOS, Web, and Desktop.
* **PostgreSQL database:**  Reliable and scalable data storage.
* **Easy deployment:** Deploy the entire application on a single VPS.

## Roadmap

* **Analytics:** Track click-through rates and gain insights into your shortened URLs.
* **QR Codes:** Generate QR codes for your shortened URLs for easy sharing.
* **Enhanced URL Management:** Implement features like custom slugs, expiration dates, and more.

## Getting Started

**Prerequisites:**

* Your own VPS (Virtual Private Server) with Linux installed

**Arch Linux Installation (Single Command):**

```bash
sudo yay -S git jdk17-openjdk docker docker-compose postgresql --noconfirm 
```

**Set JAVA_HOME for oh-my-zsh (Copy-paste this into your `~/.zshrc`):**

```bash
export JAVA_HOME="/usr/lib/jvm/java-17-openjdk"
export PATH="$JAVA_HOME/bin:$PATH"
```

**Steps:**

##### TLDR
```bash
git clone https://github.com/samoylenkodmitry/shrtlin.git && cd shrtlin && chmod +x ./deploy.sh && ./deploy.sh
```

1. Clone the repository: `git clone https://github.com/samoylenkodmitry/shrtlin.git`
2. **Ensure the prerequisites are installed and configured as described above.**
3. Build and run the application: `./deploy.sh`
4. Check environment the created `.env`

## License

This project is licensed under the [LICENSE](LICENSE).

## Contributing

We welcome contributions from the community! Please feel free to submit issues, feature requests, or pull requests to help us improve shrtlin.

```



#### File: `./docker-compose.yml`
```


version: '3.7'

services:
  backend:
    build: ./server/
    ports:
      - "8080:8080"
    secrets:
      - db_username
      - db_password
    labels:
      - "com.github.jrcs.letsencrypt_nginx_proxy_companion.nginx_proxy.VIRTUAL_HOST=shrtl.in"
      - "com.github.jrcs.letsencrypt_nginx_proxy_companion.nginx_proxy.PROXY_PASS=http://backend:8080"
  frontend:
    build: ./composeApp
    ports:
      - "80:80"
    labels:
      - "com.github.jrcs.letsencrypt_nginx_proxy_companion.nginx_proxy.VIRTUAL_HOST=shrtl.in"
      - "com.github.jrcs.letsencrypt_nginx_proxy_companion.nginx_proxy.PROXY_PASS=http://frontend:80"
  nginx-proxy:
    image: jwilder/nginx-proxy:alpine
    ports:
      - "80:80"
      - "443:443"
    volumes:
      - certbot-etc:/etc/letsencrypt
      - certbot-var:/var/www/certbot
      - dhparam:/etc/nginx/dhparam
  letsencrypt-companion:
    image: jrcs/letsencrypt-nginx-proxy-companion
    volumes:
      - certbot-etc:/etc/letsencrypt
      - certbot-var:/var/www/certbot
      - dhparam:/etc/nginx/dhparam
    depends_on:
      - nginx-proxy
    environment:
      NGINX_PROXY_CONTAINER: "nginx-proxy"
  postgres:
    image: postgres:latest
    restart: always
    environment:
      POSTGRES_USER_FILE: /run/secrets/db_username
      POSTGRES_PASSWORD_FILE: /run/secrets/db_password
      POSTGRES_DB: shrtlin
    volumes:
      - postgres_data:/var/lib/postgresql/data
    # ports: # Uncomment and expose if you need external access to the database 
    #   - "5432:5432" 

volumes:
  certbot-etc:
  certbot-var:
  dhparam:
    driver: local
    driver_opts:
      type: none
      device: /tmp/dhparam
      o: bind
  postgres_data:

```



#### File: `./server/docker-compose.yml`
```


version: '3.7'
services:
  postgres:
    image: postgres:latest
    environment:
      POSTGRES_DB: shrtlin
      POSTGRES_USER: user
      POSTGRES_PASSWORD: password
    ports:
      - "5432:5432"
volumes:
  postgres_data: {}

```



#### File: `./server/gen_jwks_from_key.sh`
```


#!/bin/sh
#https://ktor.io/docs/rsa-keys-generation.html
# Generate RSA private key (moved to gen_new_rss_key.sh)

echo "Public key: $(cat ktor.pub)"

# Path to the RSA public key
RSA_PUB_KEY_PATH="./ktor.pub"

# Check if the public key file exists
if [ ! -f "$RSA_PUB_KEY_PATH" ]; then
    echo "Public key file not found: $RSA_PUB_KEY_PATH"
    exit 1
fi

# Extract and format the modulus
MODULUS_HEX=$(openssl rsa -pubin -in "$RSA_PUB_KEY_PATH" -modulus -noout | cut -d'=' -f2)
MODULUS_BASE64=$(echo "$MODULUS_HEX" | xxd -r -p | base64 | tr -d '\n' | tr '+/' '-_' | tr -d '=')

# Extract and format the exponent
EXTRACTED_EXPONENT_HEX=$(openssl rsa -pubin -in "$RSA_PUB_KEY_PATH" -text -noout | grep "Exponent" | awk -F'[()]' '{print $2}' | sed 's/^0x//')
# Ensure the exponent hex string has even length
if [ $((${#EXTRACTED_EXPONENT_HEX} % 2)) -ne 0 ]; then
    EXTRACTED_EXPONENT_HEX="0${EXTRACTED_EXPONENT_HEX}"
fi
EXPONENT_BASE64=$(echo "$EXTRACTED_EXPONENT_HEX" | xxd -r -p | base64 | tr -d '\n' | tr '+/' '-_' | tr -d '=')

# Create the JWKS JSON
JWKS_JSON=$(cat <<EOF
{
  "keys": [
    {
      "alg": "RS512",
      "kty": "RSA",
      "e": "${EXPONENT_BASE64}",
      "kid": "shrtl.in_kid_1",
      "n": "${MODULUS_BASE64}"
    }
  ]
}
EOF
)

# Write the JWKS JSON to file
OUTPUT_DIR="./certs"
OUTPUT_FILE="$OUTPUT_DIR/jwks.json"
if [ ! -d "$OUTPUT_DIR" ]; then
    mkdir -p "$OUTPUT_DIR"
fi
echo "$JWKS_JSON" > "$OUTPUT_FILE"

# Output the generated jwks.json for verification
echo "Generated jwks.json:"
cat "$OUTPUT_FILE"

```



#### File: `./server/build.gradle.kts`
```


plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.ktor)
    application
}

group = "in.shrtl.app"
version = "1.0.0"
application {
    mainClass.set("in.shrtl.app.ApplicationKt")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=${extra["io.ktor.development"] ?: "false"}")
}

dependencies {
    implementation(projects.shared)
    implementation(libs.logback)
    implementation(libs.ktor.server.auth)
    implementation(libs.ktor.server.auth.jwt)
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.netty.jvm)
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.bundles.ktor)
    implementation(libs.bundles.exposed)
    implementation(libs.bundles.logging)
    implementation(libs.postgresql)
    implementation(libs.kotlinx.datetime)
    testImplementation(libs.ktor.server.tests)
    testImplementation(libs.kotlin.test.junit)
}


```



#### File: `./server/Dockerfile`
```


FROM openjdk:11-jre-slim

# Set working directory
WORKDIR /app

# Copy the JAR file
COPY ./build/libs/server-all.jar /app/server.jar

# Copy the JWKS file 
COPY ./certs/jwks.json /app/certs/jwks.json 

# Expose the port
EXPOSE 8080

# Entrypoint: run the jar file
ENTRYPOINT ["java", "-jar", "server.jar"]

```



#### File: `./server/gen_new_rss_key.sh`
```


#!/bin/sh
#https://ktor.io/docs/rsa-keys-generation.html
# Generate RSA private key
# openssl genpkey -algorithm rsa -pkeyopt rsa_keygen_bits:2048 -out ktor.pk8
openssl genpkey -algorithm rsa -pkeyopt rsa_keygen_bits:2048 -out ktor.key
openssl pkcs8 -topk8 -inform PEM -outform DER -nocrypt -in ktor.key -out ktor.pk8
openssl rsa -pubout -in ktor.key -out ktor.pub

```



#### File: `./server/src/main/kotlin/in/shrtl/app/Application.kt`
```


@file:Suppress("ktlint:standard:no-wildcard-imports", "ktlint:standard:package-name")

package `in`.shrtl.app

import AuthResult
import Challenge
import DEBUG
import DIFFICULTY_PREFIX
import EndpointWithArg
import GetUrlsRequest
import Greeting
import ProofOfWork
import RefreshResult
import RemoveUrlRequest
import SERVER_PORT
import UrlInfo
import UrlsResponse
import User
import challengeHash
import com.auth0.jwk.JwkProviderBuilder
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
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.collections.*
import kotlinx.datetime.*
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File
import java.security.KeyFactory
import java.security.SecureRandom
import java.security.interfaces.RSAPrivateCrtKey
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.RSAPublicKeySpec
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit
import kotlin.math.ceil

fun main() {
    initDB()
    embeddedServer(Netty, port = SERVER_PORT, host = hostName, module = Application::module)
        .start(wait = true)
}

const val CHALLENGE_EXPIRATION_TIME_MS = 60_000 // 1 minute in milliseconds

object Urls : LongIdTable() {
    val originalUrl = varchar("original_url", 2048)
    val shortUrl = varchar("short_url", 255)
    val comment = varchar("comment", 2048)
    val userId = long("user_id").references(Users.id)
    val timestamp = long("timestamp")
}

object Users : LongIdTable() {
    val nick = varchar("nick", 255)
    val timestamp = long("timestamp")
    val challenge = varchar("challenge", 2048).uniqueIndex()
}

val hostDebug = "0.0.0.0"
val hostRelease = System.getenv("DOMAIN") ?: "shrtl.in"
val hostName = if (DEBUG) hostDebug else hostRelease
val hostUrl = if (DEBUG) "http://$hostName:$SERVER_PORT" else "https://$hostName"

fun initDB() {
    Database.connect(
        url =
            if (DEBUG) {
                "jdbc:postgresql://localhost:5432/shrtlin"
            } else {
                System.getenv("DATABASE_URL")
            },
        driver = "org.postgresql.Driver",
        user =
            if (DEBUG) {
                "user"
            } else {
                System.getenv("DATABASE_USER")
            },
        password =
            if (DEBUG) {
                "password"
            } else {
                System.getenv("DATABASE_PASSWORD")
            },
    )
    transaction {
        SchemaUtils.create(Urls, Users)
    }
}

private const val CLAIM_USER_ID = "uid"
val privateKeyPath = if (DEBUG) "./server/ktor.pk8" else "/run/secrets/ktor_pk8"

// https://github.com/ktorio/ktor-documentation/blob/2.3.10/codeSnippets/snippets/auth-jwt-rs256/src/main/kotlin/com/example/Application.kt
fun Application.module() {
    install(ContentNegotiation) {
        json()
    }
    val issuer = hostUrl
    val audience = "in.shrtl.app"
    val myRealm = "in.shrtl.app"
    val jwkProvider =
        JwkProviderBuilder(issuer)
            .cached(10, 24, TimeUnit.HOURS)
            .rateLimited(10, 1, TimeUnit.MINUTES)
            .build()
    val jwtAlgorithm = loadJWTKey()
    val jwtVerifier = JWT.require(jwtAlgorithm).withIssuer(issuer).build()

    install(Authentication) {
        jwt("auth-jwt") {
            realm = myRealm
            verifier(jwkProvider) {
                acceptLeeway(3)
            }
            validate { credential ->
                if (credential.payload.audience.contains(audience)) {
                    JWTPrincipal(credential.payload)
                } else {
                    null
                }
            }
            challenge { defaultScheme, realm ->
                call.respond(HttpStatusCode.Unauthorized, "Token is not valid or expired")
            }
        }
    }
    routing {
        get("/") { call.respondText("Ktor: ${Greeting().greet()}") }
        get("/favicon.ico") {
            println("Favicon requested")
            call.respondText("")
        }
        staticFiles(".well-known", File("./server/certs"), "jwks.json")
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
                            JWT.create()
                                .withAudience(audience)
                                .withIssuer(issuer)
                                .withClaim(CLAIM_USER_ID, createdUser.id)
                                .sign(jwtAlgorithm)
                        // create JWT sessionToken
                        val sessionToken =
                            JWT.create()
                                .withAudience(audience)
                                .withIssuer(issuer)
                                .withExpiresAt(
                                    Instant.now().plus(1L, ChronoUnit.DAYS)
                                        .plus((0..1000).random().toLong(), ChronoUnit.MILLIS),
                                )
                                .withClaim(CLAIM_USER_ID, createdUser.id)
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
                    RefreshResult(
                        JWT.create()
                            .withAudience(audience)
                            .withIssuer(issuer)
                            .withClaim(CLAIM_USER_ID, user.id)
                            .sign(jwtAlgorithm),
                    )
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
                val userId = call.principal<JWTPrincipal>()?.payload?.getClaim(CLAIM_USER_ID)?.asLong()
                if (userId == null) {
                    call.respond(HttpStatusCode.Unauthorized, "User Not Found or invalid token")
                    return@post
                }
                receiveArgs(it) { shortenRequest ->
                    val orig = shortenRequest.url.trim().removePrefix("http://").removePrefix("https://")
                    transaction {
                        val urlId =
                            Urls.insert {
                                it[originalUrl] = orig
                                it[shortUrl] = orig
                                it[comment] = ""
                                it[Urls.userId] = userId
                                it[timestamp] = Clock.System.now().toEpochMilliseconds() - (0..1000).random()
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
                        )
                    }
                }?.let { call.respond(HttpStatusCode.Created, it) } ?: call.respond(
                    HttpStatusCode.BadRequest,
                    "Invalid URL",
                )
            }
            post(Endpoints.getUrls()) {
                val userId = call.principal<JWTPrincipal>()?.payload?.getClaim(CLAIM_USER_ID)?.asLong()
                if (userId == null) {
                    call.respond(HttpStatusCode.Unauthorized, "User Not Found or invalid token")
                    return@post
                }
                receiveArgs(it) { req: GetUrlsRequest ->
                    val offset = (req.page - 1).toLong() * req.pageSize.toLong()
                    val totalCount = transaction { Urls.select { Urls.userId eq userId }.count() }
                    val urls =
                        transaction {
                            Urls.select { Urls.userId eq userId }
                                .limit(req.pageSize, offset)
                                .map {
                                    UrlInfo(
                                        it[Urls.id].value,
                                        it[Urls.originalUrl],
                                        "$hostUrl/${it[Urls.shortUrl]}",
                                        it[Urls.comment],
                                        it[Urls.userId],
                                        it[Urls.timestamp],
                                    )
                                }
                                .toList()
                        }
                    UrlsResponse(urls, ceil(totalCount / req.pageSize.toDouble()).toInt())
                }?.let { call.respond(it) } ?: call.respond(HttpStatusCode.BadRequest, "Invalid pagination parameters")
            }
            post(Endpoints.removeUrl()) {
                val userId = call.principal<JWTPrincipal>()?.payload?.getClaim(CLAIM_USER_ID)?.asLong()
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
        }
        get("/{shortUrl}") {
            val shortUrl = call.parameters["shortUrl"]
            println("Redirecting: $shortUrl")
            val originalUrl =
                transaction {
                    Urls.select { Urls.shortUrl eq shortUrl!! }.singleOrNull()?.get(Urls.originalUrl)
                }
            println("Original URL: $originalUrl")
            if (originalUrl != null) {
                call.respondRedirect("http://$originalUrl")
            } else {
                call.respond(HttpStatusCode.NotFound, "URL Not Found")
            }
        }
    }
}

private fun loadJWTKey(): Algorithm {
    val keyBytes = File(privateKeyPath).readBytes()
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
        JWT.create()
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
            JWT.require(algorithm)
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
                Users.select { Users.challenge eq challenge }.map {
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
        Users.select { Users.id eq id }.map {
            User(it[Users.id].value, it[Users.nick])
        }.singleOrNull()
    }

suspend inline fun <reified A : Any, reified R : Any> RoutingContext.receiveArgs(
    e: EndpointWithArg<A, R>,
    makeResult: RoutingContext.(A) -> R?,
): R? = makeResult(call.receive<A>())

inline fun <reified A : Any, reified R : Any> Route.post(
    endpoint: EndpointWithArg<A, R>,
    crossinline body: suspend RoutingContext.(EndpointWithArg<A, R>) -> Unit,
): Route = post(endpoint.path) { body(endpoint) }


```



#### File: `./totext.sh`
```


#!/bin/bash
# Create or clear the output file
output_file="all.md"
> "$output_file"

# Append the `tree` command output with a max depth of 3
echo "Directory tree with max depth of 3:" >> "$output_file"
echo '```' >> "$output_file"
tree -L 5 -I build >> "$output_file"
echo '```' >> "$output_file"
echo -e "\n\n" >> "$output_file"

# Find and process relevant files, excluding those in /build/ directories
find . -type f \( -name "*.kt" -o -name "*.md" -o -name "*.yml" -o -name "*.kts" -o -name "*.sh" -o -name "template.env" -o -name "Dockerfile" \) ! -path "*/build/*" | while IFS= read -r file; do
  # skip the current file
  if [ "$file" == "./all.md" ]; then
    continue
  fi
  echo "#### File: \`$file"\` >> "$output_file"
  echo '```' >> "$output_file"
  echo -e "\n" >> "$output_file"
  cat "$file" >> "$output_file"
  echo -e "\n" >> "$output_file"
  echo '```' >> "$output_file"
  echo -e "\n\n" >> "$output_file"  # Adding newlines to separate file contents
done

# copy all.md contents to clipboard with xclip
cat "$output_file" | xclip -selection clipboard

```



#### File: `./build.gradle.kts`
```


plugins {
    // this is necessary to avoid the plugins to be loaded multiple times
    // in each subproject's classloader
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.jetbrainsCompose) apply false
    alias(libs.plugins.kotlinJvm) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.compose.compiler) apply false
}

```



#### File: `./settings.gradle.kts`
```


rootProject.name = "shrtlin"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        maven("https://maven.pkg.jetbrains.space/kotlin/p/wasm/experimental")
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        maven("https://maven.pkg.jetbrains.space/kotlin/p/wasm/experimental")
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
    }
}

include(":composeApp")
include(":server")
include(":shared")

```



#### File: `./runweb.sh`
```


#!/bin/bash
./gradlew :composeApp:wasmJsBrowserDevelopmentRun

```



#### File: `./shared/build.gradle.kts`
```


import org.jetbrains.kotlin.gradle.targets.js.dsl.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinx.serialization)
}

kotlin {
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser {
            commonWebpackConfig {
                devServer =
                    (devServer ?: KotlinWebpackConfig.DevServer()).apply {
                        static =
                            (static ?: mutableListOf()).apply {
                                // Serve sources to debug inside browser
                                add(project.projectDir.path)
                            }
                    }
            }
        }
    }

    androidTarget {
        compilations.all {
            kotlinOptions {
            }
        }
    }

    iosX64()
    iosArm64()
    iosSimulatorArm64()

    jvm()

    sourceSets {
        commonMain.dependencies {
            // put your Multiplatform dependencies here
            implementation(libs.kotlinx.serialization)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.okio.common)
        }
    }
}

android {
    namespace = "in.shrtl.app.shared"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
}

kotlin {
    jvmToolchain(21)
}


```



#### File: `./shared/src/iosMain/kotlin/Platform.ios.kt`
```


import platform.UIKit.UIDevice

class IOSPlatform: Platform {
    override val name: String = UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion
}

actual fun getPlatform(): Platform = IOSPlatform()

```



#### File: `./shared/src/iosMain/kotlin/UrlOpener.ios.kt`
```


import platform.UIKit.UIApplication

actual fun getUrlOpener(): UrlOpener =
    object : UrlOpener {
        override fun openUrl(url: String) {
            UIApplication.sharedApplication.openURL(NSURL(string = url))
        }
    }


```



#### File: `./shared/src/jvmMain/kotlin/Platform.jvm.kt`
```


class JVMPlatform: Platform {
    override val name: String = "Java ${System.getProperty("java.version")}"
}

actual fun getPlatform(): Platform = JVMPlatform()

```



#### File: `./shared/src/jvmMain/kotlin/UrlOpener.jvm.kt`
```


import java.awt.Desktop
import java.net.URI

actual fun getUrlOpener(): UrlOpener =
    object : UrlOpener {
        override fun openUrl(url: String) {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(URI(url))
            } else {
                println("Can't open browser")
            }
        }
    }


```



#### File: `./shared/src/androidMain/kotlin/Platform.android.kt`
```


import android.os.Build

class AndroidPlatform : Platform {
    override val name: String = "Android ${Build.VERSION.SDK_INT}"
}

actual fun getPlatform(): Platform = AndroidPlatform()

object ContextHelper {
    var currentContext: android.content.Context? = null
}


```



#### File: `./shared/src/androidMain/kotlin/UrlOpener.android.kt`
```


import android.content.Intent
import android.net.Uri

actual fun getUrlOpener(): UrlOpener =
    object : UrlOpener {
        override fun openUrl(url: String) {
            ContextHelper.currentContext?.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        }
    }


```



#### File: `./shared/src/wasmJsMain/kotlin/UrlOpener.wasmJs.kt`
```


import kotlinx.browser.window

actual fun getUrlOpener(): UrlOpener =
    object : UrlOpener {
        override fun openUrl(url: String) {
            window.open(url, "_blank")
        }
    }


```



#### File: `./shared/src/wasmJsMain/kotlin/Platform.wasmJs.kt`
```


class WasmPlatform: Platform {
    override val name: String = "Web with Kotlin/Wasm"
}

actual fun getPlatform(): Platform = WasmPlatform()

```



#### File: `./shared/src/commonMain/kotlin/Challenge.kt`
```


import okio.Buffer
import okio.ByteString

fun solveChallenge(challenge: Challenge): ProofOfWork =
    ProofOfWork(challenge.challenge, solveChallenge(challenge.challenge, challenge.prefix), challenge.prefix)

fun solveChallenge(
    challenge: String,
    prefix: String,
): String {
    var nonce = 0
    while (true) {
        val solution = "$challenge:$nonce"
        val hashResult = challengeHash(solution)
        if (hashResult.startsWith(prefix)) {
            return solution
        }
        nonce++
    }
}

fun challengeHash(input: String): String {
    val buffer = Buffer()
    buffer.writeUtf8(input)
    val bytes = buffer.readByteArray()
    if (!bytes.contentEquals(input.encodeToByteArray())) {
        throw IllegalStateException("Bytes are not equal")
    }
    return ByteString.of(*bytes).sha256().hex()
}


```



#### File: `./shared/src/commonMain/kotlin/Data.kt`
```


import kotlinx.serialization.Serializable

@Serializable
data class ShortenRequest(val url: String)

@Serializable
data class RefreshTokenRequest(val refreshToken: String)

/**
 * challenge is a signed JWT token, has timestamp inside it
 */
@Serializable
data class Challenge(val challenge: String, val prefix: String = DIFFICULTY_PREFIX)

@Serializable
data class ProofOfWork(val challenge: String, val solution: String, val prefix: String)

@Serializable
data class User(
    val id: Long,
    val nick: String,
)

@Serializable
data class AuthResult(val refreshToken: String, val sessionToken: String, val user: User)

@Serializable
data class RefreshResult(val sessionToken: String)

@Serializable
data class UrlInfo(
    val id: Long,
    val originalUrl: String,
    val shortUrl: String,
    val comment: String,
    val userId: Long,
    val timestamp: Long,
)

@Serializable
data class GetUrlsRequest(val page: Int, val pageSize: Int)

@Serializable
data class UrlsResponse(
    val urls: List<UrlInfo>,
    val totalPages: Int
)

@Serializable
data class RemoveUrlRequest(val id: Long)

```



#### File: `./shared/src/commonMain/kotlin/Constants.kt`
```


import kotlin.reflect.KClass

const val SERVER_PORT = 8080

// POW Challenge difficulty prefix bytes in hex format
const val DIFFICULTY_PREFIX = "00000"
const val DEBUG = true
val hostDebug = "0.0.0.0"
val hostRelease = "shrtl.in"
val hostName = if (DEBUG) hostDebug else hostRelease
val hostUrl = if (DEBUG) "http://$hostName:$SERVER_PORT" else "https://$hostName"

class EndpointWithArg<A : Any, R : Any>(val arg: A?, val aCls: KClass<A>, val path: String, val rCls: KClass<R>)

class Endpoint<R : Any>(val path: String, val rCls: KClass<R>)

object Endpoints {
    val powGet = Endpoint("/pow/get", Challenge::class)

    fun powPost(pow: ProofOfWork? = null) = EndpointWithArg(pow, ProofOfWork::class, "/pow/post", AuthResult::class)

    fun shorten(sr: ShortenRequest? = null) = EndpointWithArg(sr, ShortenRequest::class, "/shorten", UrlInfo::class)

    fun tokenRefresh(rt: RefreshTokenRequest? = null) =
        EndpointWithArg(rt, RefreshTokenRequest::class, "/token/refresh", RefreshResult::class)

    fun getUrls(req: GetUrlsRequest? = null) = EndpointWithArg(req, GetUrlsRequest::class, "/urls", UrlsResponse::class)

    fun removeUrl(request: RemoveUrlRequest? = null) = EndpointWithArg(request, RemoveUrlRequest::class, "/url/remove", Boolean::class)
}


```



#### File: `./shared/src/commonMain/kotlin/UrlOpener.kt`
```


interface UrlOpener {
    fun openUrl(url: String)
}

expect fun getUrlOpener(): UrlOpener


```



#### File: `./shared/src/commonMain/kotlin/Platform.kt`
```


interface Platform {
    val name: String
}

expect fun getPlatform(): Platform

```



#### File: `./shared/src/commonMain/kotlin/Greeting.kt`
```


class Greeting {
    private val platform = getPlatform()

    fun greet(): String {
        return "Hello, ${platform.name}!"
    }
}

```



#### File: `./composeApp/build.gradle.kts`
```


import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.targets.js.dsl.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.compose.compiler)
}

kotlin {
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        moduleName = "composeApp"
        browser {
            commonWebpackConfig {
                outputFileName = "composeApp.js"
                devServer =
                    (devServer ?: KotlinWebpackConfig.DevServer()).apply {
                        static =
                            (static ?: mutableListOf()).apply {
                                // Serve sources to debug inside browser
                                add(project.projectDir.path)
                            }
                    }
            }
        }
        binaries.executable()
    }

    androidTarget {
        compilations.all {
            kotlinOptions {
            }
        }
    }

    jvm("desktop")

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }

    sourceSets {
        val desktopMain by getting

        androidMain.dependencies {
            implementation(libs.compose.ui.tooling.preview)
            implementation(libs.androidx.activity.compose)
            implementation(libs.ktor.client.okhttp)
        }
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(projects.shared)
            implementation(libs.lifecycle.viewmodel.compose)
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.json)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.client.serialization)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.ktor.client.auth)
            implementation(libs.kotlinx.serialization)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.multiplatform.settings.no.arg)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.datetime)
        }
        desktopMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.ktor.client.java)
        }
        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }
        wasmJsMain.dependencies {
            implementation(libs.ktor.client.js)
        }
    }
}

android {
    namespace = "in.shrtl.app"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    sourceSets["main"].res.srcDirs("src/androidMain/res")
    sourceSets["main"].resources.srcDirs("src/commonMain/resources")

    defaultConfig {
        applicationId = "in.shrtl.app"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
    }
    dependencies {
        debugImplementation(libs.compose.ui.tooling)
    }
}

compose.desktop {
    application {
        mainClass = "MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "in.shrtl.app"
            packageVersion = "1.0.0"
        }
    }
}

compose.experimental {
    web.application {}
}

kotlin {
    jvmToolchain(21)
}


```



#### File: `./composeApp/Dockerfile`
```


FROM nginx:stable-alpine

# Copy built frontend
COPY ./build/dist/wasmJs/productionExecutable/* /usr/share/nginx/html/

# Expose port
EXPOSE 80

```



#### File: `./composeApp/src/iosMain/kotlin/MainViewController.kt`
```


import androidx.compose.ui.window.ComposeUIViewController

fun MainViewController() = ComposeUIViewController { App() }

```



#### File: `./composeApp/src/androidMain/kotlin/in/shrtl/app/MainActivity.kt`
```


package `in`.shrtl.app

import App
import ContextHelper
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            App()
        }
    }

    override fun onStart() {
        super.onStart()
        ContextHelper.currentContext = this
    }

    override fun onStop() {
        super.onStop()
        ContextHelper.currentContext = null
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}


```



#### File: `./composeApp/src/desktopMain/kotlin/main.kt`
```


import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "shrtlin",
    ) {
        App()
    }
}

```



#### File: `./composeApp/src/wasmJsMain/kotlin/main.kt`
```


import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.CanvasBasedWindow

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    CanvasBasedWindow(canvasElementId = "ComposeTarget") { App() }
}

```



#### File: `./composeApp/src/commonMain/kotlin/Storage.kt`
```


import com.russhwolf.settings.Settings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object Storage {
    private const val KEY_SESSION = "sessionToken"
    private const val KEY_REFRESH = "refreshToken"
    private val settingsStorage = Settings()
    private val cache = mutableMapOf<String, String>()

    suspend fun saveTokensToStorage(
        sessionToken: String,
        refreshToken: String,
    ) {
        withContext(Dispatchers.Default) {
            cache[KEY_SESSION] = sessionToken
            cache[KEY_REFRESH] = refreshToken
            settingsStorage.putString(KEY_SESSION, sessionToken)
            settingsStorage.putString(KEY_REFRESH, refreshToken)
        }
    }

    suspend fun loadTokensFromStorage() =
        withContext(Dispatchers.Default) {
            cache.getOrPut(KEY_SESSION) { settingsStorage.getString(KEY_SESSION, "") } to
                cache.getOrPut(KEY_REFRESH) { settingsStorage.getString(KEY_REFRESH, "") }
        }
}


```



#### File: `./composeApp/src/commonMain/kotlin/ComposeApp.kt`
```


@file:Suppress("ktlint:standard:no-wildcard-imports", "ktlint:standard:function-naming")

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.ui.tooling.preview.Preview

object Navigator {
    fun openUrl(url: String) {
        getUrlOpener().openUrl(url)
    }
}

class RepositoryModel : ViewModel() {
    suspend fun openUrl(url: String) {
        withContext(Dispatchers.Default) {
            Navigator.openUrl(url)
        }
    }

    suspend fun shorten(s: String) =
        withContext(Dispatchers.Default) {
            try {
                Api.shorten(s)
            } catch (e: Exception) {
                e.message ?: "Error $e"
                null
            }
        }

    suspend fun getUrls(page: Int, pageSize: Int): UrlsResponse =
        withContext(Dispatchers.Default) {
            try {
                Api.getUrls(page, pageSize)
            } catch (e: Exception) {
                e.message ?: "Error $e"
                UrlsResponse(emptyList(), 0)
            }
        }

    suspend fun removeUrl(urlId: Long): Boolean = // Return Boolean
        withContext(Dispatchers.Default) {
            try {
                Api.removeUrl(urlId)
            } catch (e: Exception) {
                e.message ?: "Error $e"
                false // Return false on error
            }
        }
}

@Composable
@Preview
fun App() {
    MaterialTheme {
        var showContent by remember { mutableStateOf(true) }
        val repository = remember { RepositoryModel() }
        var inputText by remember { mutableStateOf("") }
        var urlInfo by remember { mutableStateOf<UrlInfo?>(null) }
        val scope = rememberCoroutineScope()
        val showButton = remember { mutableStateOf(true) }
        val userUrls = remember { mutableStateListOf<UrlInfo>() }
        val page = remember { mutableStateOf(1) }
        val pageSize = 20 // Adjust as needed
        val totalPages = remember { mutableStateOf(1) } // Start with at least one page
        LaunchedEffect(Unit) {
            val urlsResponse = repository.getUrls(page.value, pageSize)
            userUrls.addAll(urlsResponse.urls)
            totalPages.value = urlsResponse.totalPages
        }
        Column(
            modifier =
            Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            // Define gradient brush for the TextField
            val brush =
                remember {
                    Brush.radialGradient(
                        colors = listOf(Color.Green, Color.Blue, Color.Red),
                        center = Offset(100f, 100f),
                        radius = 300f,
                    )
                }

            // Input TextField
            TextField(
                value = inputText,
                onValueChange = { inputText = it },
                textStyle = TextStyle(brush = brush),
                placeholder = { Text("Enter URL here") },
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Shorten URL button
            AnimatedVisibility(showButton.value) {
                Button(onClick = {
                    scope.launch {
                        showContent = false
                        showButton.value = false
                        urlInfo = repository.shorten(inputText)
                        urlInfo?.let { userUrls.add(0, it) }
                        showButton.value = true
                        showContent = true
                    }
                }) {
                    Text("Shrtlin me!")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Column {
                // Display the result
                AnimatedVisibility(showContent) {
                    urlInfo?.let { info ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                        ) {
                            SelectionContainer {
                                Text(
                                    text = AnnotatedString("Original URL: ${info.originalUrl}"),
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = AnnotatedString("Short URL: "),
                                    style = TextStyle(color = Color.Gray),
                                )
                                ClickableText(
                                    text = AnnotatedString(info.shortUrl),
                                    onClick = { scope.launch { repository.openUrl(info.shortUrl) } },
                                    style = TextStyle(color = Color.Blue, textDecoration = TextDecoration.Underline),
                                )
                                Spacer(modifier = Modifier.width(8.dp)) // Add space between text and button
                                // Copy to clipboard button
                                val clipboardManager = LocalClipboardManager.current
                                IconButton(onClick = {
                                    clipboardManager.setText(buildAnnotatedString { append(info.shortUrl) })
                                }) {
                                    Icon(Icons.Filled.Done, contentDescription = "Copy")
                                }
                                IconButton(onClick = {
                                    scope.launch {
                                        if (repository.removeUrl(info.id)) {
                                            userUrls.removeAll { it.id == info.id }
                                            if (urlInfo?.id == info.id) urlInfo = null
                                        }
                                    }
                                }) {
                                    Icon(
                                        Icons.Filled.Delete,
                                        contentDescription = "Remove",
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = AnnotatedString("Comment: ${info.comment}"),
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = AnnotatedString("User ID: ${info.userId}"),
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            val createdAt =
                                Instant.fromEpochMilliseconds(info.timestamp)
                                    .toLocalDateTime(TimeZone.currentSystemDefault())
                            Text(
                                text = AnnotatedString("Created at: ${createdAt.date} ${createdAt.time}"),
                            )
                        }
                    }
                }
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(userUrls) { info ->
                        UrlInfoCard(info = info, repository = repository, onUrlRemove = {
                            scope.launch {
                                if (repository.removeUrl(info.id)) {
                                    userUrls.removeAll { it.id == info.id }
                                    if (urlInfo?.id == info.id) urlInfo = null
                                }
                            }
                        })
                    }
                    item {
                        if (page.value < totalPages.value) {
                            Button(onClick = { page.value++ }) {
                                Text("Load More")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun UrlInfoCard(
    info: UrlInfo,
    repository: RepositoryModel,
    onUrlRemove: () -> Unit
) {
    Card(
        elevation = 2.dp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(), // Fill the width of the card
            horizontalArrangement = Arrangement.SpaceBetween, // Space items evenly
            verticalAlignment = Alignment.CenterVertically // Center vertically
        ) {
            Column(
                modifier = Modifier.weight(1f), // Takes up available space
                verticalArrangement = Arrangement.Center // Center vertically
            ) {
                SelectionContainer {
                    Text(
                        text = AnnotatedString("Original: ${info.originalUrl}"),
                        style = TextStyle(fontSize = 12.sp) // Smaller font size
                    )
                }
                Spacer(modifier = Modifier.height(4.dp)) // Smaller spacer
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = AnnotatedString("Short: "),
                        style = TextStyle(color = Color.Gray, fontSize = 12.sp)
                    )
                    val scope = rememberCoroutineScope()
                    ClickableText(
                        text = AnnotatedString(info.shortUrl),
                        onClick = { scope.launch { repository.openUrl(info.shortUrl) } },
                        style = TextStyle(
                            color = Color.Blue,
                            textDecoration = TextDecoration.Underline,
                            fontSize = 12.sp
                        )
                    )
                    // Remove Button
                    IconButton(onClick = onUrlRemove) {
                        Icon(Icons.Filled.Delete, contentDescription = "Remove", modifier = Modifier.size(16.dp))
                    }
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            val clipboardManager = LocalClipboardManager.current
            IconButton(onClick = {
                clipboardManager.setText(buildAnnotatedString { append(info.shortUrl) })
            }) {
                Icon(Icons.Filled.Done, contentDescription = "Copy", modifier = Modifier.size(16.dp))
            }
        }
    }
}

```



#### File: `./composeApp/src/commonMain/kotlin/Api.kt`
```


@file:Suppress("ktlint:standard:no-wildcard-imports")

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

object Api {
    private val jsonInstance =
        Json {
            isLenient = true
            ignoreUnknownKeys = true
        }
    private val authClient =
        HttpClient {
            install(ContentNegotiation) { json(jsonInstance) }
            defaultRequest { url(hostUrl) }
        }
    private val requestsClient =
        HttpClient {
            install(ContentNegotiation) { json(jsonInstance) }
            defaultRequest { url(hostUrl) }
            install(Auth) {
                bearer {
                    loadTokens {
                        val (accessToken, refreshToken) = Storage.loadTokensFromStorage()
                        if (accessToken.isNotBlank() && refreshToken.isNotBlank()) {
                            BearerTokens(
                                accessToken = accessToken,
                                refreshToken = refreshToken,
                            )
                        } else {
                            requestTokens()
                        }
                    }
                    refreshTokens {
                        oldTokens?.run {
                            BearerTokens(refreshSessionToken(refreshToken).sessionToken, refreshToken)
                        } ?: requestTokens()
                    }
                }
            }
        }

    private suspend fun requestTokens(): BearerTokens =
        with(authenticate()) {
            CoroutineScope(Dispatchers.Default).launch {
                Storage.saveTokensToStorage(sessionToken, refreshToken)
            }
            BearerTokens(
                accessToken = sessionToken,
                refreshToken = refreshToken,
            )
        }

    /**
     * Get a proof of work challenge
     *
     * @return [Challenge] with challenge and difficulty prefix
     */
    private suspend fun getPow() = authClient doGet Endpoints.powGet

    /**
     * Post a proof of work solution
     *
     * @param pow ProofOfWork, solution to the challenge
     * @return [AuthResult] with new session token
     */
    private suspend fun postPow(pow: ProofOfWork) = authClient doPost Endpoints.powPost(pow)

    /**
     * Authenticate new user
     *
     * @return [AuthResult] with new session token
     */
    private suspend fun authenticate() = postPow(solveChallenge(getPow()))

    /**
     * Refresh token
     *
     * @param refreshToken String refresh token, used to get new session token
     * @return [RefreshResult] with new session token
     */
    private suspend fun refreshSessionToken(refreshToken: String) =
        authClient doPost Endpoints.tokenRefresh(RefreshTokenRequest(refreshToken))

    /**
     * Make a short URL
     *
     * @param s URL to shorten
     * @return [UrlInfo] with short URL
     */
    suspend fun shorten(s: String): UrlInfo = requestsClient doPost Endpoints.shorten(ShortenRequest(s))


    suspend fun getUrls(page: Int, pageSize: Int): UrlsResponse =
        requestsClient doPost Endpoints.getUrls(GetUrlsRequest(page, pageSize))

    suspend fun removeUrl(urlId: Long): Boolean =
        requestsClient doPost Endpoints.removeUrl(RemoveUrlRequest(urlId))

    private suspend inline infix fun <reified T : Any> HttpClient.doGet(endpoint: Endpoint<T>): T =
        get(endpoint.path).body()

    private suspend inline infix fun <reified A : Any, reified T : Any> HttpClient.doPost(endpoint: EndpointWithArg<A, T>): T =
        post(endpoint.path) {
            contentType(ContentType.Application.Json)
            setBody(endpoint.arg)
        }.body()
}


```



#### File: `./template.env`
```


DOMAIN=shrtl.in # Replace with your domain 
DATABASE_URL=jdbc:postgresql://postgres:5432/shrtlin 
DATABASE_USER=user # Replace with your database username
DATABASE_PASSWORD=password # Replace with your database password

```



