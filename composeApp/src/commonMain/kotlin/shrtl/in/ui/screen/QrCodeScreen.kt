package shrtl.`in`.ui.screen

import UrlInfo
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.godaddy.android.colorpicker.ClassicColorPicker
import com.godaddy.android.colorpicker.HsvColor
import io.github.alexzhirkevich.qrose.options.*
import io.github.alexzhirkevich.qrose.rememberQrCodePainter
import shrtl.`in`.core.Navigator
import shrtl.`in`.ui.Theme
import shrtl.`in`.util.*
import shrtl.`in`.util.haze
import shrtl.`in`.util.shader.ICE_EFFECT
import shrtl.`in`.util.shader.shaderBackground

@Suppress("ktlint:standard:function-naming")
@Composable
fun BoxScope.QrCodeScreen(info: UrlInfo) {
    val hazeState = rememberHaze()
    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier =
                Modifier.haze(hazeState).fillMaxSize().shaderBackground(ICE_EFFECT, 0.02f),
        )
        // Back button
        IconButton(
            onClick = { Navigator.card(info) },
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
        val settings = remember { QrCodeSettings() }
        LazyColumn(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier =
                Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth(fraction = 0.7f)
                    .padding(16.dp),
        ) {
            item {
                QrCode(
                    info,
                    settings,
                    modifier =
                        Modifier
                            .width(300.dp)
                            .hc(hazeState)
                            .padding(16.dp),
                )
            }
            item { RadioGroup("QR Code Shapes", settings.qrCodeShapes, settings.qrCodeShapeInd, Modifier.hc(hazeState).fillMaxWidth()) }
            item {
                RadioGroup(
                    "Dark Pixel Shapes",
                    settings.darkPixelShapes,
                    settings.darkPixelShapeInd,
                    Modifier.hc(hazeState).fillMaxWidth(),
                )
            }
            item {
                RadioGroup(
                    "Light Pixel Shapes",
                    settings.lightPixelShapes,
                    settings.lightPixelShapeInd,
                    Modifier.hc(hazeState).fillMaxWidth(),
                )
            }
            item { RadioGroup("Ball Shapes", settings.ballShapes, settings.ballShapeInd, Modifier.hc(hazeState).fillMaxWidth()) }
            item { RadioGroup("Frame Shapes", settings.frameShapes, settings.frameShapeInd, Modifier.hc(hazeState).fillMaxWidth()) }
            item { BrushChooser("Background Brush", settings.backgroundBrushSettings, Modifier.hc(hazeState).fillMaxWidth()) }
            item { BrushChooser("Dark Pixels Brush", settings.darkBrushSettings, Modifier.hc(hazeState).fillMaxWidth()) }
            item { BrushChooser("Light Pixels Brush", settings.lightBrushSettings, Modifier.hc(hazeState).fillMaxWidth()) }
            item { BrushChooser("Frame Brush", settings.frameBrushSettings, Modifier.hc(hazeState).fillMaxWidth()) }
            item { BrushChooser("Ball Brush", settings.ballBrushSettings, Modifier.hc(hazeState).fillMaxWidth()) }
            item {
                RadioGroup("Logo Shapes", settings.logoShapes, settings.shapeInd, Modifier.hc(hazeState).fillMaxWidth(), {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { settings.haveLogo.value = !settings.haveLogo.value },
                    ) {
                        Checkbox(settings.haveLogo.value, onCheckedChange = { settings.haveLogo.value = it })
                        Text("Add logo")
                    }
                })
            }
        }
    }
}

@Suppress("ktlint:standard:function-naming")
@Composable
fun QrCode(
    info: UrlInfo,
    settings: QrCodeSettings = remember { QrCodeSettings() },
    modifier: Modifier = Modifier,
) {
    val logoPainter = rememberVectorPainter(Theme.Icons.Logo)
    Image(
        painter =
            rememberQrCodePainter(info.shortUrl + "/qr/", settings.haveLogo.value) {
                if (settings.haveLogo.value) {
                    logo {
                        painter = logoPainter
                        padding = QrLogoPadding.Natural(.05f)
                        shape = settings.logoShapes[settings.shapeInd.value].second()
                        size = 0.15f
                    }
                }
                shapes {
                    ball = settings.ballShapes[settings.ballShapeInd.value].second()
                    darkPixel = settings.darkPixelShapes[settings.darkPixelShapeInd.value].second()
                    lightPixel = settings.lightPixelShapes[settings.lightPixelShapeInd.value].second()
                    frame = settings.frameShapes[settings.frameShapeInd.value].second()
                    pattern = settings.qrCodeShapes[settings.qrCodeShapeInd.value].second()
                }
                colors {
                    dark = settings.darkBrushSettings.brush.value()
                    light = settings.lightBrushSettings.brush.value()
                    frame = settings.frameBrushSettings.brush.value()
                    ball = settings.ballBrushSettings.brush.value()
                }
            },
        contentDescription = "QR Code",
        modifier =
            modifier
                .drawBehind {
                    val brush =
                        settings.backgroundBrushSettings.brush
                            .value()
                            .brush(size = 3f, Neighbors())
                    drawRoundRect(brush = brush, cornerRadius = CornerRadius(16f), size = size)
                },
        contentScale = ContentScale.FillWidth,
    )
}

@Suppress("ktlint:standard:function-naming")
@Composable
fun <T> RadioGroup(
    title: String,
    options: Array<Pair<String, () -> T>>,
    selectedIndex: MutableState<Int>,
    modifier: Modifier = Modifier,
    vararg extra: @Composable () -> Unit = emptyArray(),
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = modifier) {
        Text(
            title,
            maxLines = 1,
        )
        LazyRow(
            verticalAlignment = Alignment.CenterVertically,
            modifier = modifier.padding(8.dp),
        ) {
            for (e in extra) item { e() }
            for ((index, pair) in options.withIndex()) {
                item {
                    Row(
                        modifier = Modifier.clickable { selectedIndex.value = index }.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(
                            selected = selectedIndex.value == index,
                            onClick = { selectedIndex.value = index },
                        )
                        Text(pair.first, maxLines = 2)
                    }
                }
            }
        }
    }
}

private fun Modifier.hc(hazeState: Haze) =
    hazeChild(
        state = hazeState,
        shape = RoundedCornerShape(16.dp),
        style =
            HazeStyle(
                blurRadius = 16.dp,
                tint = Color.White.copy(alpha = 0.4f),
            ),
    )

@Suppress("ktlint:standard:function-naming")
@Composable
fun BrushChooser(
    title: String,
    settings: BrushSettings,
    modifier: Modifier = Modifier,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = modifier) {
        Text(
            title,
            maxLines = 1,
        )
        Column(
            modifier = modifier.padding(8.dp),
        ) {
            LazyRow(verticalAlignment = Alignment.CenterVertically) {
                for (type in BrushSettings.BrushType.entries) {
                    item {
                        Row(
                            modifier = Modifier.clickable { settings.currentBrush.value = type },
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            RadioButton(
                                selected = settings.currentBrush.value == type,
                                onClick = { settings.currentBrush.value = type },
                            )
                            Text(type.name, maxLines = 2)
                        }
                    }
                }
            }
            LazyRow(verticalAlignment = Alignment.CenterVertically) {
                when (settings.currentBrush.value) {
                    BrushSettings.BrushType.Solid -> item { ColorPicker("Solid color", settings.solidColor) }

                    BrushSettings.BrushType.Linear -> {
                        item { ColorPicker("Linear color 1", settings.gradientColor1) }
                        item { ColorPicker("Linear color 2", settings.gradientColor2) }
                    }

                    BrushSettings.BrushType.Radial -> {
                        item { ColorPicker("Radial color 1", settings.radialColor1) }
                        item { ColorPicker("Radial color 2", settings.radialColor2) }
                    }
                }
            }
        }
    }
}

@Suppress("ktlint:standard:function-naming")
@Composable
fun ColorPicker(
    title: String,
    outColor: MutableState<Color>,
    modifier: Modifier = Modifier,
) {
    val showPicker = remember { mutableStateOf(false) }
    Column {
        Row(modifier = modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(title, maxLines = 1)
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier =
                    Modifier
                        .size(width = 48.dp, height = 24.dp)
                        .background(outColor.value, RoundedCornerShape(8.dp))
                        .clickable { showPicker.value = !showPicker.value },
            )
        }
        AnimatedVisibility(showPicker.value) {
            Column(modifier = modifier.size(200.dp).padding(16.dp)) {
                ClassicColorPicker(
                    color = HsvColor.from(outColor.value),
                    onColorChanged = { color: HsvColor ->
                        outColor.value = color.toColor()
                    },
                )
            }
        }
    }
}

@Stable
class BrushSettings {
    enum class BrushType { Solid, Linear, Radial }

    val solidColor = mutableStateOf(Color.Black)
    val gradientColor1 = mutableStateOf(Color.Black)
    val gradientColor2 = mutableStateOf(Color.Gray)
    val radialColor1 = mutableStateOf(Color.Black)
    val radialColor2 = mutableStateOf(Color.Gray)
    private val brushes: Map<BrushType, () -> QrBrush> =
        mapOf(
            BrushType.Solid to { QrBrush.solid(solidColor.value) },
            BrushType.Linear to {
                val gradient = Brush.linearGradient(listOf(gradientColor1.value, gradientColor2.value))
                QrBrush.brush { gradient }
            },
            BrushType.Radial to {
                val colors = listOf(radialColor1.value, radialColor2.value)
                QrBrush.brush { Brush.radialGradient(colors = colors, center = Offset(it, it), radius = it) }
            },
        )
    val currentBrush = mutableStateOf(BrushType.Solid)
    val brush = derivedStateOf { brushes[currentBrush.value] ?: { QrBrush.solid(solidColor.value) } }
}

@Stable
class QrCodeSettings {
    val haveLogo = mutableStateOf(false)
    val ballShapes: Array<Pair<String, () -> QrBallShape>> =
        arrayOf(
            "Round Corners" to { QrBallShape.roundCorners(0.1f) },
            "Circle" to { QrBallShape.circle() },
            "Square" to { QrBallShape.square() },
        )
    val ballShapeInd = mutableStateOf(0)
    val logoShapes: Array<Pair<String, () -> QrLogoShape>> =
        arrayOf(
            "Round corners" to { QrLogoShape.roundCorners(8f) },
            "Circle" to { QrLogoShape.circle() },
        )
    val shapeInd = mutableStateOf(0)
    val darkPixelShapes: Array<Pair<String, () -> QrPixelShape>> =
        arrayOf(
            "Round Corners" to { QrPixelShape.roundCorners() },
            "Square" to { QrPixelShape.square() },
            "Circle" to { QrPixelShape.circle() },
            "Vertical lines" to { QrPixelShape.verticalLines() },
            "Horizontal lines" to { QrPixelShape.horizontalLines() },
        )
    val darkPixelShapeInd = mutableStateOf(0)
    val lightPixelShapes: Array<Pair<String, () -> QrPixelShape>> =
        arrayOf(
            "Round Corners" to { QrPixelShape.roundCorners() },
            "Square" to { QrPixelShape.square() },
            "Circle" to { QrPixelShape.circle() },
            "Vertical lines" to { QrPixelShape.verticalLines() },
            "Horizontal lines" to { QrPixelShape.horizontalLines() },
        )
    val lightPixelShapeInd = mutableStateOf(0)
    val qrCodeShapes: Array<Pair<String, () -> QrCodeShape>> =
        arrayOf(
            "Square" to { QrCodeShape.Default },
            "Circle" to { QrCodeShape.circle() },
            "Hexagon" to { QrCodeShape.hexagon() },
        )
    val qrCodeShapeInd = mutableStateOf(0)
    val frameShapes: Array<Pair<String, () -> QrFrameShape>> =
        arrayOf(
            "Round Corners" to { QrFrameShape.roundCorners(0.1f) },
            "Square" to { QrFrameShape.square() },
            "Circle" to { QrFrameShape.circle() },
        )
    val frameShapeInd = mutableStateOf(0)
    val darkBrushSettings = BrushSettings()
    val lightBrushSettings = BrushSettings().apply { solidColor.value = Color.White }
    val frameBrushSettings = BrushSettings()
    val ballBrushSettings = BrushSettings()
    val backgroundBrushSettings = BrushSettings().apply { solidColor.value = Color.White }
}
