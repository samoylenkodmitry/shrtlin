@file:Suppress("ktlint:standard:no-wildcard-imports", "ktlint:standard:function-naming")

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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

    suspend fun getUrls(page: Int, pageSize: Int): UrlsResponse =
        withContext(Dispatchers.Default) {
            try {
                Api.getUrls(page, pageSize)
            } catch (e: Exception) {
                e.message ?: "Error $e"
                UrlsResponse(emptyList(), 0)
            }
        }

    suspend fun removeUrl(urlId: Long): Boolean = // Return Boolean
        withContext(Dispatchers.Default) {
            try {
                Api.removeUrl(urlId)
            } catch (e: Exception) {
                e.message ?: "Error $e"
                false // Return false on error
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
        Column(
            modifier =
            Modifier
                .fillMaxWidth()
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

            Column {
                // Display the result
                AnimatedVisibility(showContent) {
                    urlInfo?.let { info ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
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
                                    style = TextStyle(color = Color.Blue, textDecoration = TextDecoration.Underline),
                                )
                                Spacer(modifier = Modifier.width(8.dp)) // Add space between text and button
                                // Copy to clipboard button
                                val clipboardManager = LocalClipboardManager.current
                                IconButton(onClick = {
                                    clipboardManager.setText(buildAnnotatedString { append(info.shortUrl) })
                                }) {
                                    Icon(Icons.Filled.Done, contentDescription = "Copy")
                                }
                                IconButton(onClick = {
                                    scope.launch {
                                        if (repository.removeUrl(info.id)) {
                                            userUrls.removeAll { it.id == info.id }
                                            if (urlInfo?.id == info.id) urlInfo = null
                                        }
                                    }
                                }) {
                                    Icon(
                                        Icons.Filled.Delete,
                                        contentDescription = "Remove",
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
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
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
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

@Composable
fun UrlInfoCard(
    info: UrlInfo,
    repository: RepositoryModel,
    onUrlRemove: () -> Unit
) {
    Card(
        elevation = 2.dp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(), // Fill the width of the card
            horizontalArrangement = Arrangement.SpaceBetween, // Space items evenly
            verticalAlignment = Alignment.CenterVertically // Center vertically
        ) {
            Column(
                modifier = Modifier.weight(1f), // Takes up available space
                verticalArrangement = Arrangement.Center // Center vertically
            ) {
                SelectionContainer {
                    Text(
                        text = AnnotatedString("Original: ${info.originalUrl}"),
                        style = TextStyle(fontSize = 12.sp) // Smaller font size
                    )
                }
                Spacer(modifier = Modifier.height(4.dp)) // Smaller spacer
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = AnnotatedString("Short: "),
                        style = TextStyle(color = Color.Gray, fontSize = 12.sp)
                    )
                    val scope = rememberCoroutineScope()
                    ClickableText(
                        text = AnnotatedString(info.shortUrl),
                        onClick = { scope.launch { repository.openUrl(info.shortUrl) } },
                        style = TextStyle(
                            color = Color.Blue,
                            textDecoration = TextDecoration.Underline,
                            fontSize = 12.sp
                        )
                    )
                    // Remove Button
                    IconButton(onClick = onUrlRemove) {
                        Icon(Icons.Filled.Delete, contentDescription = "Remove", modifier = Modifier.size(16.dp))
                    }
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            val clipboardManager = LocalClipboardManager.current
            IconButton(onClick = {
                clipboardManager.setText(buildAnnotatedString { append(info.shortUrl) })
            }) {
                Icon(Icons.Filled.Done, contentDescription = "Copy", modifier = Modifier.size(16.dp))
            }
        }
    }
}