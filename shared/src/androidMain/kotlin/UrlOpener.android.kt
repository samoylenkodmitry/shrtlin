import android.content.Intent
import android.net.Uri

actual fun getUrlOpener(): UrlOpener =
    object : UrlOpener {
        override fun openUrl(url: String) {
            ContextHelper.currentContext?.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        }
    }
