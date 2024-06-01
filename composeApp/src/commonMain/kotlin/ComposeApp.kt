@file:Suppress("ktlint:standard:no-wildcard-imports", "ktlint:standard:function-naming")
@file:OptIn(ExperimentalFoundationApi::class)

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Edit
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
import kotlinx.coroutines.Dispatchers
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

    data object Login : Screen

    data class Error(val message: String) : Screen

    data class Card(val info: UrlInfo) : Screen
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

    fun login() {
        screenFlow.tryEmit(Screen.Login)
    }

    fun cardScreen(info: UrlInfo) {
        screenFlow.tryEmit(Screen.Card(info))
    }
}

sealed interface AuthState {
    data object Unauthenticated : AuthState

    data object Authenticating : AuthState

    data class Authenticated(val auth: AuthResult) : AuthState

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

    suspend fun doLogin(userId: String): Boolean =
        withContext(Dispatchers.Default + SupervisorJob()) {
            try {
                Api.doLogin(userId)
            } catch (e: Exception) {
                e.message ?: "Error $e"
                false
            }
        }

    suspend fun updateNick(newNick: String): Boolean =
        withContext(Dispatchers.Default + SupervisorJob()) {
            try {
                Api.updateNick(newNick)
            } catch (e: Exception) {
                e.message ?: "Error $e"
                false
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

        Box(modifier = Modifier.fillMaxSize()) {
            when (val screen = screenState.value) {
                Screen.Splash -> SplashScreen()
                Screen.Main -> MainScreen()
                Screen.UserProfile -> UserProfileScreen()
                Screen.Login -> LoginScreen()
                is Screen.Card -> CardScreen(screen.info)
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
                is Notification.Info ->
                    Text(
                        n.message,
                        modifier = Modifier.padding(8.dp),
                        color = Color.hsl(120f, 0.7f, 0.9f),
                    )

                is Notification.Error ->
                    Text(
                        n.message,
                        modifier = Modifier.padding(8.dp),
                        color = Color.hsl(0f, 0.7f, 0.9f),
                    )
            }
        }
    }
}

@Composable
fun BoxScope.ErrorScreen(message: String) {
    Column(modifier = Modifier.align(Alignment.Center)) {
        Text("Error: $message")
        Button(onClick = {
            Navigator.main()
        }) {
            Text("Back")
        }
    }
}

@Composable
fun BoxScope.UserProfileScreen() {
    val authState = AppGraph.auth.collectAsState(AuthState.Unauthenticated)
    val scope = rememberCoroutineScope()

    IconButton(
        onClick = { Navigator.main() },
        modifier = Modifier.align(Alignment.TopStart).padding(16.dp),
    ) {
        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
    }

    // Column for user profile info
    Column(
        modifier = Modifier.align(Alignment.Center).padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Placeholder for a circle (e.g., user avatar)
        Box(modifier = Modifier.size(100.dp).background(Color.Gray, shape = CircleShape)) {
            // User icon
            Icon(
                Theme.Icons.User,
                contentDescription = "User",
                modifier = Modifier.align(Alignment.Center),
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // User ID
        when (val state = authState.value) {
            is AuthState.Authenticated -> {
                Column {
                    val userId = state.auth.user.id
                    Text("User #: $userId")
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
                                            if (Repository.updateNick(newNick)) {
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
                        Spacer(modifier = Modifier.width(8.dp))
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
                Modifier
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
                            shadowElevation = if (showRemoveButton.value) 8f else 0f
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
                            Button(onClick = { scope.launch { Repository.logout() } }) {
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

@Composable
fun BoxScope.LoginScreen() {
    // Back button
    IconButton(
        onClick = { Navigator.main() },
        modifier = Modifier.align(Alignment.TopStart).padding(16.dp),
    ) {
        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
    }
    Column(modifier = Modifier.align(Alignment.Center)) {
        // Text field for user ID
        var userId by remember { mutableStateOf("") }
        Box {
            TextField(
                value = userId,
                onValueChange = { userId = it },
                label = { Text("User ID") },
            )
            val clipboardManager = LocalClipboardManager.current
            val scope = rememberCoroutineScope()
            // Paste button
            IconButton(
                onClick = {
                    scope.launch {
                        clipboardManager.getText()?.let { userId = it.text }
                    }
                },
                modifier = Modifier.align(Alignment.CenterEnd),
            ) {
                Icon(Theme.Icons.Clipboard, contentDescription = "Paste")
            }
        }
        // Button to login
        val scope = rememberCoroutineScope()
        Button(onClick = {
            scope.launch {
                if (!Repository.doLogin(userId)) {
                    AppGraph.notifications.tryEmit(Notification.Error("Could not login"))
                } else {
                    AppGraph.notifications.tryEmit(Notification.Info("Logged in"))
                    Navigator.userProfile()
                }
            }
        }, modifier = Modifier.padding(36.dp).align(Alignment.CenterHorizontally)) {
            Text("Login")
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
    val scope = rememberCoroutineScope()
    val showButton = remember { mutableStateOf(true) }
    val userUrls = remember { mutableStateListOf<UrlInfo>() }
    val page = remember { mutableStateOf(1) }
    val totalPages = remember { mutableStateOf(1) }
    LaunchedEffect(Unit) {
        val urlsResponse = Repository.getUrls(page.value, 20)
        userUrls.addAll(urlsResponse.urls)
        totalPages.value = urlsResponse.totalPages
    }
    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(top = 16.dp),
    ) {
        ButtonUser(Modifier.align(Alignment.TopEnd).padding(top = 20.dp, end = 20.dp))
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
                        val urlInfo = Repository.shorten(inputText)
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

            Column(modifier = Modifier.fillMaxSize()) {
                LazyColumn(
                    modifier =
                        Modifier
                            .align(Alignment.CenterHorizontally),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(userUrls) { info ->
                        UrlInfoCard(
                            info = info,
                            onUrlRemove = {
                                scope.launch {
                                    if (Repository.removeUrl(info.id)) {
                                        userUrls.removeAll { it.id == info.id }
                                        AppGraph.notifications.tryEmit(Notification.Info("URL removed"))
                                    } else {
                                        AppGraph.notifications.tryEmit(Notification.Error("Could not remove URL"))
                                    }
                                }
                            },
                            modifier =
                                Modifier
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
                }
            }
        }
    }
}

@Composable
private fun BoxScope.CardScreen(info: UrlInfo) {
    val scope = rememberCoroutineScope()
    // back button
    IconButton(
        onClick = { Navigator.main() },
        modifier = Modifier.align(Alignment.TopStart).padding(16.dp),
    ) {
        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
    }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.align(Alignment.Center),
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
            ButtonDelete(onClick = {
                scope.launch {
                    if (Repository.removeUrl(info.id)) {
                        AppGraph.notifications.tryEmit(Notification.Info("URL removed"))
                    } else {
                        AppGraph.notifications.tryEmit(Notification.Error("Could not remove URL"))
                    }
                }
            })
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

@Composable
private fun ButtonDelete(onClick: () -> Unit) {
    IconButton(onClick = onClick) {
        Icon(
            Theme.Icons.Trash2,
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
            Theme.Icons.User,
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
    modifier: Modifier = Modifier,
) {
    Card(
        elevation = 2.dp,
        modifier =
            modifier
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
