@file:Suppress("ktlint:standard:no-wildcard-imports", "ktlint:standard:function-naming")

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.*
import androidx.compose.material.icons.filled.Delete
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
import androidx.lifecycle.ViewModel
import compose.icons.FeatherIcons
import compose.icons.feathericons.Clipboard
import compose.icons.feathericons.Delete
import compose.icons.feathericons.UserMinus
import kotlinx.coroutines.*
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.ui.tooling.preview.Preview

object Navigator {
    fun openUrl(url: String) {
        getUrlOpener().openUrl(url)
    }
}

class RepositoryModel : ViewModel() {
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
        withContext(Dispatchers.Default) {
            try {
                Api.logout()
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
        var showContent by remember { mutableStateOf(true) }
        val repository = remember { RepositoryModel() }
        var inputText by remember { mutableStateOf("") }
        var urlInfo by remember { mutableStateOf<UrlInfo?>(null) }
        val scope = rememberCoroutineScope()
        val showButton = remember { mutableStateOf(true) }
        val userUrls = remember { mutableStateListOf<UrlInfo>() }
        val page = remember { mutableStateOf(1) }
        val pageSize = 20 // Adjust as needed
        val totalPages = remember { mutableStateOf(1) } // Start with at least one page
        LaunchedEffect(Unit) {
            val urlsResponse = repository.getUrls(page.value, pageSize)
            userUrls.addAll(urlsResponse.urls)
            totalPages.value = urlsResponse.totalPages
        }
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(16.dp),
        ) {
            ButtonLogout(repository, Modifier.align(Alignment.TopEnd))
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
                            urlInfo = repository.shorten(inputText)
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
                                repository = repository,
                                deleteUrl = {
                                    scope.launch {
                                        urlInfo?.let { info ->
                                            if (repository.removeUrl(info.id)) {
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
                            UrlInfoCard(info = info, repository = repository, onUrlRemove = {
                                scope.launch {
                                    if (repository.removeUrl(info.id)) {
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
}

@Composable
private fun ColumnScope.ResultNewUrlCard(
    showContent: Boolean,
    urlInfo: UrlInfo?,
    scope: CoroutineScope,
    repository: RepositoryModel,
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
                        onClick = { scope.launch { repository.openUrl(info.shortUrl) } },
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
            FeatherIcons.Delete,
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
        Icon(FeatherIcons.Clipboard, contentDescription = "Copy")
    }
}

@Composable
private fun ButtonLogout(
    repository: RepositoryModel,
    modifier: Modifier = Modifier,
) {
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
    IconButton(onClick = {
        scope.launch {
            animateRotation = true
            repository.logout()
            animateRotation = false
        }
    }, modifier = modifier) {
        Icon(
            FeatherIcons.UserMinus,
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
    repository: RepositoryModel,
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
                        onClick = { scope.launch { repository.openUrl(info.shortUrl) } },
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
