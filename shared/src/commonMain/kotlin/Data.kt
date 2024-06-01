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
    var nick: String,
)

@Serializable
data class AuthResult(val refreshToken: String, val sessionToken: String, val user: User)

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
    val totalPages: Int,
)

@Serializable
data class RemoveUrlRequest(val id: Long)

@Serializable
data class UpdateNickRequest(val nick: String)
