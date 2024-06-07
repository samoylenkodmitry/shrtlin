@file:Suppress("ktlint:standard:no-wildcard-imports", "ktlint:standard:function-naming")
@file:OptIn(ExperimentalFoundationApi::class)

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.haze
import dev.chrisbanes.haze.hazeChild
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.ui.tooling.preview.Preview
import shader.*

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

    suspend fun getClicks(
        urlId: Long,
        period: Period,
    ): UrlStats? =
        withContext(Dispatchers.Default) {
            try {
                Api.getClicks(urlId, period)
            } catch (e: Exception) {
                e.message ?: "Error $e"
                null
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
fun Logo(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.padding(start = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = rememberVectorPainter(Theme.Icons.Logo),
            contentDescription = "Logo",
            modifier = Modifier,
        )
        Text(
            text = "hrtlin",
            style = TextStyle(fontSize = 46.sp),
            color = Color(0xFF333355),
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Bold,
            modifier =
                Modifier.graphicsLayer {
                    translationX = -45f
                },
        )
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
                    modifier = Modifier.fillMaxWidth().padding(8.dp).background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(16.dp)),
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

@Composable
fun BoxScope.ErrorScreen(message: String) {
    Column(
        modifier =
            Modifier.align(Alignment.Center)
                .shaderBackground(BLACK_CHERRY_COSMOS_2_PLUS_EFFECT, 0.05f),
    ) {
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
    val hazeState = remember { HazeState() }
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
                        Modifier.padding(16.dp).size(100.dp)
                            .background(Color.Gray.copy(0.2f), shape = CircleShape),
                ) {
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
    }
}

@Composable
fun BoxScope.LoginScreen() {
    val hazeState = remember { HazeState() }
    Box(
        modifier =
            Modifier.fillMaxSize(),
    ) {
        Box(
            modifier =
                Modifier.fillMaxSize().haze(
                    hazeState,
                ).shaderBackground(ICE_EFFECT, 0.2f),
        )
        // Back button
        IconButton(
            onClick = { Navigator.userProfile() },
            modifier =
                Modifier.align(Alignment.TopStart).padding(16.dp)
                    .hazeChild(
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
            modifier =
                Modifier.align(Alignment.Center).padding(16.dp)
                    .hazeChild(
                        state = hazeState,
                        shape = RoundedCornerShape(16.dp),
                        style =
                            HazeStyle(
                                blurRadius = 16.dp,
                                tint = Color.White.copy(alpha = 0.4f),
                            ),
                    ),
        ) {
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
}

@Composable
private fun SplashScreen() {
    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .shaderBackground(ICE_EFFECT, 0.1f)
                .padding(16.dp),
    ) {
        Column(modifier = Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
            Logo()
        }
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
    val hazeState = remember { HazeState() }
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
                                    if (Repository.removeUrl(info.id)) {
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

@Composable
private fun BoxScope.CardScreen(info: UrlInfo) {
    val scope = rememberCoroutineScope()
    var selectedPeriod by remember { mutableStateOf(Period.DAY) }
    var clicksData by remember { mutableStateOf<UrlStats?>(null) }

    // Fetch clicks data based on selected period
    LaunchedEffect(info.id, selectedPeriod) {
        clicksData = Repository.getClicks(info.id, selectedPeriod)
    }

    val hazeState = remember { HazeState() }
    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier =
                Modifier.haze(state = hazeState).fillMaxSize().shaderBackground(ICE_EFFECT, 0.02f),
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
                Modifier.hazeChild(
                    state = hazeState,
                    shape = RoundedCornerShape(16.dp),
                    style =
                        HazeStyle(
                            blurRadius = 16.dp,
                            tint = Color.White.copy(alpha = 0.4f),
                        ),
                ).align(Alignment.Center).padding(16.dp),
        ) {
            SelectionContainer {
                Text(text = AnnotatedString("Original URL: ${info.originalUrl}"))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = AnnotatedString("Short URL: "), style = TextStyle(color = Color.Gray))
                ClickableText(
                    text = AnnotatedString(info.shortUrl),
                    onClick = { scope.launch { Repository.openUrl(info.shortUrl) } },
                    style = TextStyle(color = Color.Blue, textDecoration = TextDecoration.Underline),
                )
                Spacer(modifier = Modifier.width(8.dp))
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

@Composable
fun SimpleLineChart(urlStats: UrlStats) {
    val maxClicks = urlStats.clickCounts.maxOrNull() ?: 0
    val yScale = if (maxClicks > 0) 200f / maxClicks else 1f

    val textMeasurer = rememberTextMeasurer()
    val justText = true
    if (justText) {
        LazyColumn {
            urlStats.clicks.forEach { (date, clicks) ->
                // header
                item {
                    Row {
                        Text(
                            text = "Date",
                            modifier = Modifier.padding(start = 10.dp),
                        )
                        Text(
                            text = "Clicks",
                            modifier = Modifier.padding(start = 10.dp),
                        )
                    }
                }

                item {
                    Row {
                        Text(
                            text = timestampToDate(date.toLong()),
                            modifier = Modifier.padding(start = 10.dp),
                        )
                        Text(
                            text = clicks.toString(),
                            modifier = Modifier.padding(start = 10.dp),
                        )
                    }
                }
            }
        }
        return
    }
    Canvas(modifier = Modifier.fillMaxWidth().height(200.dp)) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        val barWidth = canvasWidth / urlStats.dates.size

        urlStats.clickCounts.forEachIndexed { index, clickCount ->
            val barHeight = clickCount * yScale
            val left = index * barWidth
            drawRect(
                color = Color.Blue,
                topLeft = Offset(left, canvasHeight - barHeight),
                size = Size(barWidth, barHeight),
            )
        }

        urlStats.dates.forEachIndexed { index, date ->
            val x = index * barWidth + barWidth / 2
            drawText(
                textMeasurer = textMeasurer,
                text = date,
                topLeft = Offset(x, canvasHeight),
            )
        }
    }
}

private fun timestampToDate(date: Long): String {
    val instant = Instant.fromEpochMilliseconds(date)
    val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
    return "${localDateTime.date} ${localDateTime.time}"
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
