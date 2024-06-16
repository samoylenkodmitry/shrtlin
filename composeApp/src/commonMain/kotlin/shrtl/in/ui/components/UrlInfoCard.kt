package shrtl.`in`.ui.components

import UrlInfo
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import shrtl.`in`.core.ViewModel

@Suppress("ktlint:standard:function-naming")
@Composable
fun UrlInfoCard(
    info: UrlInfo,
    onUrlRemove: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 4.dp),
    ) {
        Row(
            modifier =
                Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
            // Fill the width with the card
            horizontalArrangement = Arrangement.SpaceBetween, // Space items evenly
            verticalAlignment = Alignment.CenterVertically, // Center vertically
        ) {
            Column(
                modifier = Modifier.weight(1f), // Takes up available space
                verticalArrangement = Arrangement.Center, // Center vertically
            ) {
                SelectionContainer {
                    Text(
                        text = AnnotatedString("Original: ${info.originalUrl}"),
                        style = TextStyle(fontSize = 12.sp), // Smaller font size
                    )
                }
                Spacer(modifier = Modifier.height(4.dp)) // Smaller spacer
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = AnnotatedString("Short: "),
                        style = TextStyle(color = Color.Gray, fontSize = 12.sp),
                    )
                    val scope = rememberCoroutineScope()
                    ClickableText(
                        text = AnnotatedString(info.shortUrl),
                        onClick = { scope.launch { ViewModel.openUrl(info.shortUrl) } },
                        style =
                            TextStyle(
                                color = Color.Blue,
                                textDecoration = TextDecoration.Underline,
                                fontSize = 12.sp,
                            ),
                    )
                    ButtonDelete(onUrlRemove)
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = AnnotatedString("${info.clicks + info.qrClicks} clicks"),
                style = TextStyle(fontSize = 12.sp),
            )
            Spacer(modifier = Modifier.width(8.dp))
            ButtonCopyToClipboard(info.shortUrl)
        }
    }
}
