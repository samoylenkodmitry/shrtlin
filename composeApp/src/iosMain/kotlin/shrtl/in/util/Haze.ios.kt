package shrtl.`in`.util

import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.haze
import dev.chrisbanes.haze.hazeChild

actual fun createHaze(): Haze = HazeImpl()

class HazeImpl : Haze {
    private val state = HazeState()

    override fun Modifier.haze(): Modifier = this.haze(state)

    override fun Modifier.hazeChild(
        shape: Shape,
        style: HazeStyle,
    ): Modifier =
        hazeChild(
            state = state,
            shape = shape,
            style =
                dev.chrisbanes.haze.HazeStyle(
                    blurRadius = style.blurRadius,
                    tint = style.tint,
                ),
        )
}
