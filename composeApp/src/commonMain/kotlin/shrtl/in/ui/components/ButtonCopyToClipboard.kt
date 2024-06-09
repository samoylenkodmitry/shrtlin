package shrtl.`in`.ui.components

import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.buildAnnotatedString
import shrtl.`in`.ui.Theme

@Composable
fun ButtonCopyToClipboard(textToCopy: String) {
    val clipboardManager = LocalClipboardManager.current
    IconButton(onClick = {
        clipboardManager.setText(buildAnnotatedString { append(textToCopy) })
    }) {
        Icon(Theme.Icons.Clipboard, contentDescription = "Copy")
    }
}
