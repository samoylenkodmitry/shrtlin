import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.CanvasBasedWindow
import kotlinx.browser.document
import org.w3c.dom.get

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    document.getElementsByClassName("loader-container")[0]?.remove()
    CanvasBasedWindow(canvasElementId = "ComposeTarget") { App() }
}
