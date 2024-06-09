package shrtl.`in`.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import shrtl.`in`.core.AppGraph
import shrtl.`in`.core.data.Notification
import shrtl.`in`.util.shader.*

@Suppress("ktlint:standard:function-naming")
@Composable
fun NotificationPopup() {
    val scope = rememberCoroutineScope()
    var notification by remember { mutableStateOf<Notification?>(null) }
    LaunchedEffect(Unit) {
        AppGraph.notifications.collect { n ->
            notification = n
            scope.launch {
                delay(n.duration.toLong())
                notification = null
            }
        }
    }
    notification?.let { n ->
        Box(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier =
                    Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .shaderBackground(
                            listOf(
                                GLOSSY_GRADIENTS_EFFECT,
                                INK_FLOW_EFFECT,
                                BLACK_CHERRY_COSMOS_2_PLUS_EFFECT,
                                ICE_EFFECT,
                            ).random(),
                            0.2f,
                        )
                        .width(300.dp).height(100.dp).align(Alignment.BottomEnd),
            ) {
                Row(
                    modifier =
                        Modifier.fillMaxWidth().padding(8.dp)
                            .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(16.dp)),
                ) {
                    Box(modifier = Modifier.align(Alignment.CenterVertically).weight(1f).padding(start = 8.dp)) {
                        when (n) {
                            is Notification.Info ->
                                Text(
                                    n.message,
                                    modifier = Modifier.padding(8.dp),
                                    color = Color.White,
                                )

                            is Notification.Error ->
                                Text(
                                    n.message,
                                    modifier = Modifier.padding(8.dp),
                                    color = Color.White,
                                )
                        }
                    }
                    IconButton(modifier = Modifier.padding(8.dp), onClick = { notification = null }) {
                        Icon(Icons.Filled.Done, contentDescription = "Dismiss", tint = Color.White)
                    }
                }
            }
        }
    }
}
