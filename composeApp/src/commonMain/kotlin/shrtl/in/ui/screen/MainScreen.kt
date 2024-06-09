@file:OptIn(ExperimentalFoundationApi::class)

package shrtl.`in`.ui.screen

import UrlInfo
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import shrtl.`in`.core.AppGraph
import shrtl.`in`.core.Navigator
import shrtl.`in`.core.ViewModel
import shrtl.`in`.core.data.Notification
import shrtl.`in`.ui.components.ButtonUser
import shrtl.`in`.ui.components.Logo
import shrtl.`in`.ui.components.UrlInfoCard
import shrtl.`in`.util.HazeStyle
import shrtl.`in`.util.haze
import shrtl.`in`.util.hazeChild
import shrtl.`in`.util.rememberHaze
import shrtl.`in`.util.shader.ICE_EFFECT
import shrtl.`in`.util.shader.shaderBackground

@Composable
fun MainScreen() {
    var showContent by remember { mutableStateOf(true) }
    var inputText by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val showButton = remember { mutableStateOf(true) }
    val userUrls = remember { mutableStateListOf<UrlInfo>() }
    val page = remember { mutableStateOf(1) }
    val totalPages = remember { mutableStateOf(1) }
    LaunchedEffect(Unit) {
        val urlsResponse = ViewModel.getUrls(page.value, 20)
        userUrls.addAll(urlsResponse.urls)
        totalPages.value = urlsResponse.totalPages
    }
    val hazeState = rememberHaze()
    Box(
        modifier =
            Modifier
                .fillMaxSize(),
    ) {
        Box(
            modifier =
                Modifier.haze(state = hazeState).fillMaxSize().shaderBackground(ICE_EFFECT, 0.009f),
        )
        ButtonUser(
            Modifier.align(Alignment.TopEnd).padding(top = 20.dp, end = 20.dp)
                .hazeChild(
                    state = hazeState,
                    shape = CircleShape,
                    style =
                        HazeStyle(
                            blurRadius = 16.dp,
                            tint = Color.White.copy(alpha = 0.4f),
                        ),
                ),
        )
        Column(
            modifier =
                Modifier
                    .fillMaxWidth(fraction = 0.7f)
                    .align(Alignment.Center)
                    .padding(top = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            // Define gradient brush for the TextField
            val brush =
                remember {
                    Brush.radialGradient(
                        colors = listOf(Color.Green, Color.Blue, Color.Red),
                        center = Offset(100f, 100f),
                        radius = 300f,
                    )
                }

            Logo(modifier = Modifier.padding(16.dp))
            // Input TextField
            TextField(
                value = inputText,
                onValueChange = { inputText = it },
                textStyle = TextStyle(brush = brush),
                placeholder = { Text("Enter URL here") },
                colors = TextFieldDefaults.textFieldColors(backgroundColor = Color.Transparent),
                modifier =
                    Modifier.fillMaxWidth().hazeChild(
                        state = hazeState,
                        shape = RoundedCornerShape(16.dp),
                        style =
                            HazeStyle(
                                blurRadius = 16.dp,
                                tint = Color.White.copy(alpha = 0.4f),
                            ),
                    ),
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Shorten URL button
            AnimatedVisibility(showButton.value) {
                Button(onClick = {
                    scope.launch {
                        showContent = false
                        showButton.value = false
                        val urlInfo = ViewModel.shorten(inputText)
                        if (urlInfo == null) {
                            AppGraph.notifications.tryEmit(Notification.Error("Could not shorten URL"))
                        }
                        urlInfo?.let { userUrls.add(0, it) }
                        showButton.value = true
                        showContent = true
                    }
                }) {
                    Text("Shrtlin me!")
                }
            }

            Spacer(modifier = Modifier.height(36.dp))

            Box(
                modifier =
                    Modifier.align(Alignment.CenterHorizontally),
            ) {
                val scrollState = rememberLazyListState()
                LazyColumn(
                    state = scrollState,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .align(Alignment.TopCenter),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(userUrls) { info ->
                        UrlInfoCard(
                            info = info,
                            onUrlRemove = {
                                scope.launch {
                                    if (ViewModel.removeUrl(info.id)) {
                                        userUrls.removeAll { it.id == info.id }
                                        AppGraph.notifications.tryEmit(Notification.Info("URL removed"))
                                    } else {
                                        AppGraph.notifications.tryEmit(Notification.Error("Could not remove URL"))
                                    }
                                }
                            },
                            modifier =
                                Modifier.hazeChild(
                                    state = hazeState,
                                    shape = RoundedCornerShape(16.dp),
                                    style =
                                        HazeStyle(
                                            blurRadius = 16.dp,
                                            tint = Color.White.copy(alpha = 0.4f),
                                        ),
                                )
                                    .animateItemPlacement()
                                    .clickable { Navigator.cardScreen(info) },
                        )
                    }
                    item {
                        if (page.value < totalPages.value) {
                            Button(onClick = { page.value++ }) {
                                Text("Load More")
                            }
                        }
                    }
                    item {
                        Spacer(modifier = Modifier.height(48.dp))
                    }
                }
            }
        }
    }
}
