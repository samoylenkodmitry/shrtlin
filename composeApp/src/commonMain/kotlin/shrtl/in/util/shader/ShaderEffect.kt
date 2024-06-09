package shrtl.`in`.util.shader

import androidx.compose.animation.core.withInfiniteAnimationFrameMillis
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.onGloballyPositioned

interface ShaderEffect {
    val supported: Boolean
    val ready: State<Boolean>

    fun updateUniforms(
        time: Float,
        width: Float,
        height: Float,
    )

    fun brush(): Brush
}

expect fun build(shaderCode: String): ShaderEffect

abstract class CommonNonAndroidShaderEffect(shaderCode: String) : ShaderEffect {
    override val supported: Boolean = true
    override var ready: MutableState<Boolean> = mutableStateOf(false)

    override fun updateUniforms(
        time: Float,
        width: Float,
        height: Float,
    ) {
        setUniforms(time, width, height)
        ready.value = width > 0 && height > 0
    }

    abstract fun setUniforms(
        time: Float,
        width: Float,
        height: Float,
    )
}

fun Modifier.shaderBackground(
    shaderCode: String,
    speed: Float,
): Modifier =
    composed {
        val shaderEffect = remember(shaderCode) { build(shaderCode) }
        if (!shaderEffect.supported) return@composed Modifier

        val time =
            produceState(0f) {
                while (true) {
                    withInfiniteAnimationFrameMillis {
                        value = speed * (it / 16.6f) / 10f
                    }
                }
            }
        var size by remember { mutableStateOf(Size(0f, 0f)) }
        shaderEffect.updateUniforms(time.value, size.width, size.height)

        Modifier.onGloballyPositioned {
            size = Size(it.size.width.toFloat(), it.size.height.toFloat())
        }.drawBehind {
            if (shaderEffect.ready.value) {
                drawRect(brush = shaderEffect.brush())
            }
        }
    }
