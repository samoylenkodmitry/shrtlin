import kotlinx.browser.window

actual fun getUrlOpener(): UrlOpener =
    object : UrlOpener {
        override fun openUrl(url: String) {
            window.open(url, "_blank")
        }
    }
