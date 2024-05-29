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
                            requestAndSaveNewTokens()
                        }
                    }
                    refreshTokens {
                        oldTokens?.run {
                            BearerTokens(refreshSessionToken(refreshToken).sessionToken, refreshToken)
                        } ?: requestAndSaveNewTokens()
                    }
                }
            }
        }

    private suspend fun requestAndSaveNewTokens(): BearerTokens =
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

    suspend fun getUrls(
        page: Int,
        pageSize: Int,
    ): UrlsResponse = requestsClient doPost Endpoints.getUrls(GetUrlsRequest(page, pageSize))

    suspend fun removeUrl(urlId: Long): Boolean = requestsClient doPost Endpoints.removeUrl(RemoveUrlRequest(urlId))

    private suspend inline infix fun <reified T : Any> HttpClient.doGet(endpoint: Endpoint<T>): T = get(endpoint.path).body()

    private suspend inline infix fun <reified A : Any, reified T : Any> HttpClient.doPost(endpoint: EndpointWithArg<A, T>): T =
        post(endpoint.path) {
            contentType(ContentType.Application.Json)
            setBody(endpoint.arg)
        }.body()

    suspend fun logout() {
        Storage.clearData()
        requestAndSaveNewTokens()
    }
}
