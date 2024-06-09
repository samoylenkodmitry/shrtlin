package shrtl.`in`.util

import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp

expect fun createHaze(): Haze

@Composable
fun rememberHaze() = remember(::createHaze)

interface Haze {
    fun Modifier.haze(): Modifier

    fun Modifier.hazeChild(
        shape: Shape,
        style: HazeStyle,
    ): Modifier
}

class HazeNoOp : Haze {
    override fun Modifier.haze(): Modifier = this

    override fun Modifier.hazeChild(
        shape: Shape,
        style: HazeStyle,
    ): Modifier = clip(shape).background(color = style.tint.copy(alpha = 0.9f))
}

fun Modifier.haze(state: Haze): Modifier = state.run { haze() }

fun Modifier.hazeChild(
    state: Haze,
    shape: Shape,
    style: HazeStyle,
): Modifier = state.run { hazeChild(shape, style) }

class HazeStyle(var blurRadius: Dp, var tint: Color)
