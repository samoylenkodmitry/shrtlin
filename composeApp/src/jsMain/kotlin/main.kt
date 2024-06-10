import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.CanvasBasedWindow
import kotlinx.browser.document
import org.jetbrains.skiko.wasm.onWasmReady
import org.w3c.dom.get

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    onWasmReady {
        document.getElementsByClassName("loader-container")[0]?.remove()
        CanvasBasedWindow("composeTarget") {
            App()
        }
    }
}
