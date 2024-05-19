interface UrlOpener {
    fun openUrl(url: String)
}

expect fun getUrlOpener(): UrlOpener
