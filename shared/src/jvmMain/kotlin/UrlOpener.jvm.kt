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
