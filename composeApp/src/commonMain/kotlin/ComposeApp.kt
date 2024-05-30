@file:Suppress("ktlint:standard:no-wildcard-imports", "ktlint:standard:function-naming")

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.ui.tooling.preview.Preview

sealed interface Screen {
    data object Splash : Screen

    data object Main : Screen

    data object UserProfile : Screen

    data class Error(val message: String) : Screen
}

object Navigator {
    val screenFlow = MutableSharedFlow<Screen>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)

    fun openUrl(url: String) {
        getUrlOpener().openUrl(url)
    }

    fun main() {
        screenFlow.tryEmit(Screen.Main)
    }

    fun userProfile() {
        screenFlow.tryEmit(Screen.UserProfile)
    }

    fun error(message: String) {
        screenFlow.tryEmit(Screen.Error(message))
    }

    fun splash() {
        screenFlow.tryEmit(Screen.Splash)
    }
}

sealed interface AuthState {
    data object Unauthenticated : AuthState

    data object Authenticating : AuthState

    data class Authenticated(val id: String) : AuthState

    data object AuthError : AuthState
}

sealed class Notification(val duration: Int) {
    data class Error(val message: String) : Notification(duration = 5000)

    data class Info(val message: String) : Notification(duration = 3000)
}

object AppGraph {
    val auth =
        MutableSharedFlow<AuthState>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST).apply {
            tryEmit(AuthState.Unauthenticated)
        }
    val notifications = MutableSharedFlow<Notification>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
}

object Repository {
    suspend fun openUrl(url: String) {
        withContext(Dispatchers.Default) {
            Navigator.openUrl(url)
        }
    }

    suspend fun shorten(s: String) =
        withContext(Dispatchers.Default) {
            try {
                Api.shorten(s)
            } catch (e: Exception) {
                e.message ?: "Error $e"
                null
            }
        }

    suspend fun getUrls(
        page: Int,
        pageSize: Int,
    ): UrlsResponse =
        withContext(Dispatchers.Default) {
            try {
                Api.getUrls(page, pageSize)
            } catch (e: Exception) {
                e.message ?: "Error $e"
                UrlsResponse(emptyList(), 0)
            }
        }

    suspend fun removeUrl(urlId: Long): Boolean =
        withContext(Dispatchers.Default) {
            try {
                Api.removeUrl(urlId)
            } catch (e: Exception) {
                e.message ?: "Error $e"
                false // Return false on error
            }
        }

    suspend fun logout() {
        withContext(Dispatchers.Default + SupervisorJob()) {
            try {
                Api.logout()
            } catch (e: Exception) {
                e.message ?: "Error $e"
            }
        }
    }

    suspend fun checkAuth() {
        withContext(Dispatchers.Default + SupervisorJob()) {
            try {
                Api.checkAuth()
            } catch (e: Exception) {
                e.message ?: "Error $e"
                UrlsResponse(emptyList(), 0)
            }
        }
    }
}

@Composable
@Preview
fun App() {
    MaterialTheme {
        val screenState = Navigator.screenFlow.collectAsState(Screen.Main)
        val scope = rememberCoroutineScope()
        LaunchedEffect(Unit) {
            scope.launch {
                Repository.checkAuth()
            }
            scope.launch {
                AppGraph.auth.collect { authState ->
                    when (authState) {
                        is AuthState.Authenticated -> {
                            AppGraph.notifications.tryEmit(Notification.Info("Hello!"))
                            Navigator.main()
                        }

                        AuthState.AuthError -> {
                            Navigator.error("Could not authenticate")
                        }

                        AuthState.Authenticating -> {
                            Navigator.splash()
                        }

                        AuthState.Unauthenticated -> {
                            Navigator.splash()
                        }
                    }
                }
            }
        }

        Box {
            when (val screen = screenState.value) {
                Screen.Splash -> SplashScreen()
                Screen.Main -> MainScreen()
                Screen.UserProfile -> UserProfileScreen()
                is Screen.Error -> ErrorScreen(screen.message)
            }
            Box(modifier = Modifier.width(300.dp).height(100.dp).align(Alignment.BottomEnd)) {
                NotificationPopup()
            }
        }
    }
}

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
        Snackbar(
            modifier = Modifier.padding(16.dp),
            action = {
                Button(onClick = { notification = null }) {
                    Text("Dismiss")
                }
            },
        ) {
            when (n) {
                is Notification.Info -> Text(n.message, modifier = Modifier.padding(8.dp), color = Color.Green)
                is Notification.Error -> Text(n.message, modifier = Modifier.padding(8.dp), color = Color.Red)
            }
        }
    }
}

@Composable
fun ErrorScreen(message: String) {
    Column {
        Text("Error: $message")
        Button(onClick = {
            Navigator.main()
        }) {
            Text("Back")
        }
    }
}

@Composable
fun UserProfileScreen() {
    val authState = AppGraph.auth.collectAsState(AuthState.Unauthenticated)

    Column {
        Text("User Profile")
        SelectionContainer {
            when (val state = authState.value) {
                is AuthState.Authenticated -> {
                    Text("User ID: ${state.id}")
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
        }
        // Button to copy id to clipboard
        val state = authState.value
        if (state is AuthState.Authenticated) {
            val clipboardManager = LocalClipboardManager.current
            Button(onClick = {
                clipboardManager.setText(buildAnnotatedString { append(state.id) })
            }) {
                Text("Copy ID to clipboard")
            }
        }

        Button(onClick = {
            Navigator.main()
        }) {
            Text("Go main")
        }
        val scope = rememberCoroutineScope()
        Button(onClick = {
            scope.launch {
                Repository.logout()
            }
        }) {
            Text("Logout")
        }
    }
}

@Composable
private fun SplashScreen() {
    val hueAnimation = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        while (true) {
            hueAnimation.animateTo(
                targetValue = if (hueAnimation.value > 0) 0f else 360f,
                animationSpec =
                    infiniteRepeatable(
                        animation = tween(durationMillis = 20000),
                    ),
            )
        }
    }
    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(
                    color =
                        Color.hsl(
                            hue = hueAnimation.value,
                            saturation = 0.1f,
                            lightness = 0.7f,
                        ),
                )
                .padding(16.dp),
    ) {
        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
    }
}

@Composable
private fun MainScreen() {
    var showContent by remember { mutableStateOf(true) }
    var inputText by remember { mutableStateOf("") }
    var urlInfo by remember { mutableStateOf<UrlInfo?>(null) }
    val scope = rememberCoroutineScope()
    val showButton = remember { mutableStateOf(true) }
    val userUrls = remember { mutableStateListOf<UrlInfo>() }
    val page = remember { mutableStateOf(1) }
    val pageSize = 20 // Adjust as needed
    val totalPages = remember { mutableStateOf(1) } // Start with at least one page
    LaunchedEffect(Unit) {
        val urlsResponse = Repository.getUrls(page.value, pageSize)
        userUrls.addAll(urlsResponse.urls)
        totalPages.value = urlsResponse.totalPages
    }
    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(16.dp),
    ) {
        ButtonUser(Modifier.align(Alignment.TopEnd))
        Column(
            modifier =
                Modifier
                    .fillMaxWidth(fraction = 0.7f)
                    .align(Alignment.Center)
                    .padding(16.dp),
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

            // Input TextField
            TextField(
                value = inputText,
                onValueChange = { inputText = it },
                textStyle = TextStyle(brush = brush),
                placeholder = { Text("Enter URL here") },
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Shorten URL button
            AnimatedVisibility(showButton.value) {
                Button(onClick = {
                    scope.launch {
                        showContent = false
                        showButton.value = false
                        urlInfo = Repository.shorten(inputText)
                        urlInfo?.let { userUrls.add(0, it) }
                        showButton.value = true
                        showContent = true
                    }
                }) {
                    Text("Shrtlin me!")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Column(modifier = Modifier.fillMaxSize()) {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    item {
                        // Display the result
                        ResultNewUrlCard(
                            showContent = showContent,
                            urlInfo = urlInfo,
                            scope = scope,
                            deleteUrl = {
                                scope.launch {
                                    urlInfo?.let { info ->
                                        if (Repository.removeUrl(info.id)) {
                                            userUrls.removeAll { it.id == info.id }
                                            if (urlInfo?.id == info.id) urlInfo = null
                                        }
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                    items(userUrls) { info ->
                        UrlInfoCard(info = info, onUrlRemove = {
                            scope.launch {
                                if (Repository.removeUrl(info.id)) {
                                    userUrls.removeAll { it.id == info.id }
                                    if (urlInfo?.id == info.id) urlInfo = null
                                }
                            }
                        })
                    }
                    item {
                        if (page.value < totalPages.value) {
                            Button(onClick = { page.value++ }) {
                                Text("Load More")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ColumnScope.ResultNewUrlCard(
    showContent: Boolean,
    urlInfo: UrlInfo?,
    scope: CoroutineScope,
    deleteUrl: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AnimatedVisibility(showContent, modifier = modifier) {
        urlInfo?.let { info ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth(),
            ) {
                SelectionContainer {
                    Text(
                        text = AnnotatedString("Original URL: ${info.originalUrl}"),
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = AnnotatedString("Short URL: "),
                        style = TextStyle(color = Color.Gray),
                    )
                    ClickableText(
                        text = AnnotatedString(info.shortUrl),
                        onClick = { scope.launch { Repository.openUrl(info.shortUrl) } },
                        style =
                            TextStyle(
                                color = Color.Blue,
                                textDecoration = TextDecoration.Underline,
                            ),
                    )
                    Spacer(modifier = Modifier.width(8.dp)) // Add space between text and button
                    ButtonCopyToClipboard(info.shortUrl)
                    ButtonDelete(deleteUrl)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = AnnotatedString("Comment: ${info.comment}"),
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = AnnotatedString("User ID: ${info.userId}"),
                )
                Spacer(modifier = Modifier.height(8.dp))
                val createdAt =
                    Instant.fromEpochMilliseconds(info.timestamp)
                        .toLocalDateTime(TimeZone.currentSystemDefault())
                Text(
                    text = AnnotatedString("Created at: ${createdAt.date} ${createdAt.time}"),
                )
            }
        }
    }
}

@Composable
private fun ButtonDelete(onClick: () -> Unit) {
    IconButton(onClick = onClick) {
        Icon(
            Theme.Icons.Delete,
            contentDescription = "Delete",
            modifier = Modifier.size(16.dp),
        )
    }
}

@Composable
private fun ButtonCopyToClipboard(textToCopy: String) {
    val clipboardManager = LocalClipboardManager.current
    IconButton(onClick = {
        clipboardManager.setText(buildAnnotatedString { append(textToCopy) })
    }) {
        Icon(Theme.Icons.Clipboard, contentDescription = "Copy")
    }
}

@Composable
private fun ButtonUser(modifier: Modifier = Modifier) {
    var rTarget by remember { mutableFloatStateOf(0f) }
    val animRotation by animateFloatAsState(
        targetValue = rTarget,
        animationSpec = spring(),
        label = "Rotation",
    )
    val scope = rememberCoroutineScope()
    var animateRotation by remember { mutableStateOf(false) }

    LaunchedEffect(animateRotation) {
        while (animateRotation) {
            rTarget += (-360..360).random()
            delay(500)
        }
        rTarget = 0f
    }
    LaunchedEffect(Unit) {
        AppGraph.auth.collect { authState ->
            when (authState) {
                is AuthState.Authenticated -> {
                    animateRotation = false
                }

                AuthState.AuthError -> {
                    animateRotation = false
                }

                AuthState.Authenticating -> {
                    animateRotation = true
                }

                AuthState.Unauthenticated -> {
                }
            }
        }
    }
    IconButton(onClick = {
        scope.launch {
            Navigator.userProfile()
        }
    }, modifier = modifier) {
        Icon(
            Theme.Icons.UserMinus,
            contentDescription = "Logout",
            modifier =
                Modifier.graphicsLayer {
                    rotationZ = animRotation
                },
        )
    }
}

@Composable
fun UrlInfoCard(
    info: UrlInfo,
    onUrlRemove: () -> Unit,
) {
    Card(
        elevation = 2.dp,
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
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
                        onClick = { scope.launch { Repository.openUrl(info.shortUrl) } },
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
            ButtonCopyToClipboard(info.shortUrl)
        }
    }
}
