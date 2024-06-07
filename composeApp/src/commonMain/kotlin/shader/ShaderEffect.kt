package shader

import androidx.compose.animation.core.withInfiniteAnimationFrameMillis
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.layout.onGloballyPositioned
import org.jetbrains.skia.RuntimeShaderBuilder

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

class NonAndroidShaderEffect(shaderCode: String) : ShaderEffect {
    private val shaderBuilder = RuntimeShaderBuilder(org.jetbrains.skia.RuntimeEffect.makeForShader(shaderCode))
    override val supported: Boolean = true
    override var ready: MutableState<Boolean> = mutableStateOf(false)

    override fun updateUniforms(
        time: Float,
        width: Float,
        height: Float,
    ) {
        shaderBuilder.uniform("iResolution", width, height, width / height)
        shaderBuilder.uniform("iTime", time)
        ready.value = width > 0 && height > 0
    }

    override fun brush(): Brush = ShaderBrush(shaderBuilder.makeShader())
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
