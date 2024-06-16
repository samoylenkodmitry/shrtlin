package shrtl.`in`.ui.screen

import Period
import UrlInfo
import UrlStats
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import shrtl.`in`.core.AppGraph
import shrtl.`in`.core.Navigator
import shrtl.`in`.core.ViewModel
import shrtl.`in`.core.data.Notification
import shrtl.`in`.ui.components.ButtonCopyToClipboard
import shrtl.`in`.ui.components.ButtonDelete
import shrtl.`in`.ui.components.SimpleLineChart
import shrtl.`in`.util.HazeStyle
import shrtl.`in`.util.haze
import shrtl.`in`.util.hazeChild
import shrtl.`in`.util.rememberHaze
import shrtl.`in`.util.shader.ICE_EFFECT
import shrtl.`in`.util.shader.shaderBackground

@Suppress("ktlint:standard:function-naming")
@Composable
fun BoxScope.CardScreen(info: UrlInfo) {
    val scope = rememberCoroutineScope()
    var selectedPeriod by remember { mutableStateOf(Period.DAY) }
    var clicksData by remember { mutableStateOf<UrlStats?>(null) }

    // Fetch clicks data based on selected period
    LaunchedEffect(info.id, selectedPeriod) {
        clicksData = ViewModel.getClicks(info.id, selectedPeriod)
    }

    val hazeState = rememberHaze()
    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier =
                Modifier.haze(hazeState).fillMaxSize().shaderBackground(ICE_EFFECT, 0.02f),
        )
        // Back button
        IconButton(
            onClick = { Navigator.main() },
            modifier =
                Modifier.padding(16.dp).align(Alignment.TopStart).hazeChild(
                    state = hazeState,
                    shape = CircleShape,
                    style =
                        HazeStyle(
                            blurRadius = 16.dp,
                            tint = Color.White.copy(alpha = 0.4f),
                        ),
                ),
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier =
                Modifier
                    .hazeChild(
                        state = hazeState,
                        shape = RoundedCornerShape(16.dp),
                        style =
                            HazeStyle(
                                blurRadius = 16.dp,
                                tint = Color.White.copy(alpha = 0.4f),
                            ),
                    ).align(Alignment.Center)
                    .padding(16.dp),
        ) {
            SelectionContainer {
                Text(text = AnnotatedString("Original URL: ${info.originalUrl}"))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = AnnotatedString("Clicks: ${info.clicks}"))
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = AnnotatedString("QR Clicks: ${info.qrClicks}"))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = AnnotatedString("Short URL: "), style = TextStyle(color = Color.Gray))
                ClickableText(
                    text = AnnotatedString(info.shortUrl),
                    onClick = { scope.launch { ViewModel.openUrl(info.shortUrl) } },
                    style = TextStyle(color = Color.Blue, textDecoration = TextDecoration.Underline),
                )
                Spacer(modifier = Modifier.width(8.dp))
                ButtonCopyToClipboard(info.shortUrl)
                ButtonDelete(onClick = {
                    scope.launch {
                        if (ViewModel.removeUrl(info.id)) {
                            AppGraph.notifications.tryEmit(Notification.Info("URL removed"))
                        } else {
                            AppGraph.notifications.tryEmit(Notification.Error("Could not remove URL"))
                        }
                    }
                })
                IconButton(onClick = { Navigator.qrCode(info) }) {
                    QrCode(
                        info,
                        modifier = Modifier.width(60.dp),
                        settings =
                            remember {
                                QrCodeSettings().apply {
                                    backgroundBrushSettings.solidColor.value = Color.Transparent
                                    lightBrushSettings.solidColor.value = Color.Transparent
                                } 
                            },
                    )
                }
            }
            // Spacer(modifier = Modifier.height(8.dp))
            // Text(text = AnnotatedString("Comment: ${info.comment}")) // todo modify comments
            Spacer(modifier = Modifier.height(8.dp))
            val createdAt =
                Instant.fromEpochMilliseconds(info.timestamp).toLocalDateTime(TimeZone.currentSystemDefault())
            Text(text = AnnotatedString("Created at: ${createdAt.date} ${createdAt.time}"))

            Spacer(modifier = Modifier.height(16.dp))

            // Period selection buttons
            Row(modifier = Modifier.padding(16.dp).scale(0.8f).width(500.dp)) {
                Button(onClick = { selectedPeriod = Period.MINUTE }, modifier = Modifier.weight(1f)) {
                    Text("Minute")
                }
                Button(onClick = { selectedPeriod = Period.HOUR }, modifier = Modifier.weight(1f)) {
                    Text("Hour")
                }
                Button(onClick = { selectedPeriod = Period.DAY }, modifier = Modifier.weight(1f)) {
                    Text("Day")
                }
                Button(onClick = { selectedPeriod = Period.MONTH }, modifier = Modifier.weight(1f)) {
                    Text("Month")
                }
                Button(onClick = { selectedPeriod = Period.YEAR }, modifier = Modifier.weight(1f)) {
                    Text("Year")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Display chart (using a simple line chart for now)
            clicksData?.let {
                SimpleLineChart(it)
            }
        }
    }
}
