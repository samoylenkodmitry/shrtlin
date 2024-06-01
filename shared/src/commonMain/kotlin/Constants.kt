import kotlin.reflect.KClass

// POW Challenge difficulty prefix bytes in hex format
const val DIFFICULTY_PREFIX = "0000"
const val IS_LOCALHOST = false // Set to true to run locally
val SERVER_PORT = if (IS_LOCALHOST) 8080 else 80
val hostDebug = "0.0.0.0"
val hostRelease = "shrtl.in"
val hostName = if (IS_LOCALHOST) hostDebug else hostRelease
val hostUrl = if (IS_LOCALHOST) "http://$hostName:$SERVER_PORT" else "https://$hostName"

class EndpointWithArg<A : Any, R : Any>(val arg: A?, val aCls: KClass<A>, val path: String, val rCls: KClass<R>)

class Endpoint<R : Any>(val path: String, val rCls: KClass<R>)

object Endpoints {
    val powGet = Endpoint("/pow/get", Challenge::class)

    fun powPost(pow: ProofOfWork? = null) = EndpointWithArg(pow, ProofOfWork::class, "/pow/post", AuthResult::class)

    fun shorten(sr: ShortenRequest? = null) = EndpointWithArg(sr, ShortenRequest::class, "/shorten", UrlInfo::class)

    fun tokenRefresh(rt: RefreshTokenRequest? = null) = EndpointWithArg(rt, RefreshTokenRequest::class, "/token/refresh", AuthResult::class)

    fun getUrls(req: GetUrlsRequest? = null) = EndpointWithArg(req, GetUrlsRequest::class, "/urls", UrlsResponse::class)

    fun removeUrl(request: RemoveUrlRequest? = null) = EndpointWithArg(request, RemoveUrlRequest::class, "/url/remove", Boolean::class)

    fun updateNick(request: UpdateNickRequest? = null) = EndpointWithArg(request, UpdateNickRequest::class, "/user/nick", Boolean::class)
}
