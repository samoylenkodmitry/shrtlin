import platform.Foundation.NSURL
import platform.UIKit.UIApplication

actual fun getUrlOpener(): UrlOpener =
    object : UrlOpener {
        override fun openUrl(url: String) {
            UIApplication.sharedApplication.openURL(NSURL(string = url))
        }
    }
