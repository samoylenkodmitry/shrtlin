package shrtl.`in`.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import shrtl.`in`.core.AppGraph
import shrtl.`in`.core.Navigator
import shrtl.`in`.core.ViewModel
import shrtl.`in`.core.data.AuthState
import shrtl.`in`.core.data.Notification
import shrtl.`in`.ui.Theme
import shrtl.`in`.util.HazeStyle
import shrtl.`in`.util.haze
import shrtl.`in`.util.hazeChild
import shrtl.`in`.util.rememberHaze
import shrtl.`in`.util.shader.ICE_EFFECT
import shrtl.`in`.util.shader.shaderBackground

@Composable
fun BoxScope.UserProfileScreen() {
    val hazeState = rememberHaze()
    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier =
                Modifier.haze(state = hazeState).fillMaxSize().shaderBackground(ICE_EFFECT, 0.02f),
        )
        val authState = AppGraph.auth.collectAsState(AuthState.Unauthenticated)
        val scope = rememberCoroutineScope()

        IconButton(
            onClick = { Navigator.main() },
            modifier =
                Modifier.align(Alignment.TopStart).padding(16.dp).hazeChild(
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

        Box(
            modifier =
                Modifier.align(
                    Alignment.Center,
                ).hazeChild(
                    state = hazeState,
                    shape = RoundedCornerShape(16.dp),
                    style =
                        HazeStyle(
                            blurRadius = 16.dp,
                            tint = Color.White.copy(alpha = 0.4f),
                        ),
                ).padding(16.dp),
        ) {
            // Column for user profile info
            Column(
                modifier =
                Modifier,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // Placeholder for a circle (e.g., user avatar)
                Box(
                    modifier =
                        androidx.compose.ui.Modifier.padding(16.dp).size(100.dp)
                            .background(Color.Gray.copy(0.2f), shape = CircleShape),
                ) {
                    // User icon
                    Icon(
                        Theme.Icons.User,
                        contentDescription = "User",
                        modifier = Modifier.align(Alignment.Center),
                    )
                }

                Spacer(modifier = androidx.compose.ui.Modifier.height(16.dp))

                // User ID
                when (val state = authState.value) {
                    is AuthState.Authenticated -> {
                        Column {
                            val nick = state.auth.user.nick
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                val editMode = remember { mutableStateOf(false) }
                                Text("Nick:")
                                Box {
                                    if (editMode.value) {
                                        var newNick by remember { mutableStateOf(nick) }
                                        TextField(
                                            value = newNick,
                                            onValueChange = { newNick = it },
                                            label = { Text("New nick") },
                                        )
                                        IconButton(
                                            onClick = {
                                                scope.launch {
                                                    if (ViewModel.updateNick(newNick)) {
                                                        AppGraph.notifications.tryEmit(Notification.Info("Nick updated"))
                                                        editMode.value = false
                                                    } else {
                                                        AppGraph.notifications.tryEmit(Notification.Error("Could not update nick"))
                                                    }
                                                }
                                            },
                                            modifier = Modifier.align(Alignment.CenterEnd),
                                        ) {
                                            Icon(Icons.Filled.Done, contentDescription = "Edit nick")
                                        }
                                    } else {
                                        Text(nick)
                                    }
                                }
                                // Button to edit nick
                                IconButton(onClick = { editMode.value = !editMode.value }) {
                                    Icon(Icons.Filled.Edit, contentDescription = "Edit nick")
                                }
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("ID: ${state.auth.refreshToken.take(8)}...${state.auth.refreshToken.takeLast(8)}")
                                Spacer(modifier = androidx.compose.ui.Modifier.width(8.dp))
                                val clipboardManager = LocalClipboardManager.current
                                IconButton(onClick = {
                                    clipboardManager.setText(AnnotatedString(state.auth.refreshToken))
                                    AppGraph.notifications.tryEmit(Notification.Info("ID copied to clipboard"))
                                }) {
                                    Icon(Theme.Icons.Clipboard, contentDescription = "Copy ID to clipboard")
                                }
                            }
                        }
                    }

                    AuthState.AuthError -> {
                        Text("Error")
                    }

                    AuthState.Authenticating -> {
                        Text("Authenticating")
                    }

                    AuthState.Unauthenticated -> {
                        Text("Unauthenticated")
                    }
                }
                Row(
                    modifier =
                        androidx.compose.ui.Modifier
                            .padding(36.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Button(onClick = { Navigator.login() }) {
                        Text("Login by ID")
                    }

                    val showRemoveButton = remember { mutableStateOf(false) }
                    Column(
                        modifier =
                            Modifier
                                .graphicsLayer {
                                    rotationZ = if (showRemoveButton.value) -20f else 0f
                                    shadowElevation = if (showRemoveButton.value) 4f else 0f
                                }
                                .padding(top = if (showRemoveButton.value) 20.dp else 0.dp),
                    ) {
                        Button(
                            onClick = { showRemoveButton.value = true },
                            modifier = Modifier.align(Alignment.CenterHorizontally),
                        ) {
                            Text("New identity")
                        }
                        AnimatedVisibility(showRemoveButton.value) {
                            Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 5.dp)) {
                                Text("Are you sure?", modifier = Modifier.align(Alignment.CenterHorizontally))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Button(onClick = { scope.launch { ViewModel.logout() } }) {
                                        Text("Yes", color = Color.Red)
                                    }
                                    Button(onClick = { showRemoveButton.value = false }) {
                                        Text("No", color = Color.Green)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
