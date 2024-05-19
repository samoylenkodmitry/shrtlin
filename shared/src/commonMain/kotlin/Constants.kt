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
