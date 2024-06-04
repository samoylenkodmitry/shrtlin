import kotlin.reflect.KClass

// POW Challenge difficulty prefix bytes in hex format
const val DIFFICULTY_PREFIX = "0000"
const val IS_LOCALHOST = false // Set to true to run locally
val SERVER_PORT = if (IS_LOCALHOST) 8080 else 80
val hostDebug = "0.0.0.0"
val hostRelease = "shrtl.in"
val hostName = if (IS_LOCALHOST) hostDebug else hostRelease
val hostUrl = if (IS_LOCALHOST) "http://$hostName:$SERVER_PORT" else "https://$hostName"
const val FULL_ICON =
    "data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMjIwIiBoZWlnaHQ9IjEwMCIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj4KICA8IS0tIEJhY2tncm91bmQgLS0+CiAgPHJlY3Qgd2lkdGg9IjEwMCUiIGhlaWdodD0iMTAwJSIgZmlsbD0id2hpdGUiLz4KICAKICA8IS0tIFJldmVyc2VkICdTJyBMaW5rIEljb24gLS0+CiAgPGcgZmlsbD0ibm9uZSI+CiAgICA8cGF0aCBkPSJNNDAsMzAgaDIwIGExMCwxMCAwIDAgMSAwLDIwIGgtMTAgYTEwLDEwIDAgMCAwIDAsMjAgaDIwIiBzdHJva2U9IiMwMDdCRkYiIHN0cm9rZS13aWR0aD0iNiIvPgogIDwvZz4KCiAgPCEtLSBUZXh0IC0tPgogIDx0ZXh0IHg9IjgwIiB5PSI2NSIgZm9udC1mYW1pbHk9IkFyaWFsLCBzYW5zLXNlcmlmIiBmb250LXNpemU9IjQ2IiBmaWxsPSIjMzMzMzU1Ij5ocnRsaW48L3RleHQ+Cjwvc3ZnPg=="
const val SQUARE_ICON =
    "data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMTEwIiBoZWlnaHQ9IjEwMCIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj48cmVjdCB3aWR0aD0iMTAwJSIgaGVpZ2h0PSIxMDAlIiBmaWxsPSIjZmZmIi8+PHBhdGggZD0iTTQwIDMwaDIwYTEwIDEwIDAgMCAxIDAgMjBINTBhMTAgMTAgMCAwIDAgMCAyMGgyMCIgc3Ryb2tlPSIjMDA3QkZGIiBzdHJva2Utd2lkdGg9IjYiIGZpbGw9Im5vbmUiLz48L3N2Zz4="
const val SQUARE_ICON_DATA =
    "PHN2ZyB3aWR0aD0iMTEwIiBoZWlnaHQ9IjEwMCIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj48cmVjdCB3aWR0aD0iMTAwJSIgaGVpZ2h0PSIxMDAlIiBmaWxsPSIjZmZmIi8+PHBhdGggZD0iTTQwIDMwaDIwYTEwIDEwIDAgMCAxIDAgMjBINTBhMTAgMTAgMCAwIDAgMCAyMGgyMCIgc3Ryb2tlPSIjMDA3QkZGIiBzdHJva2Utd2lkdGg9IjYiIGZpbGw9Im5vbmUiLz48L3N2Zz4="

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

    fun getClicks(req: GetClicksRequest? = null) = EndpointWithArg(req, GetClicksRequest::class, "/url/clicks", UrlStats::class)

    fun getAllClicks(req: GetClicksRequest? = null) = EndpointWithArg(req, GetClicksRequest::class, "/url/clicks", UrlStats::class)
}
