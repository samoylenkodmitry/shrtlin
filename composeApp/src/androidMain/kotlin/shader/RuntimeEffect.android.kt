package shader

import android.graphics.RuntimeShader
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ShaderBrush

internal class NoOpShaderEffect : ShaderEffect {
    override val supported: Boolean = false
    override var ready: MutableState<Boolean> = mutableStateOf(false)

    override fun updateUniforms(
        time: Float,
        width: Float,
        height: Float,
    ) = Unit

    override fun brush(): Brush = Brush.horizontalGradient(listOf(Color.Transparent, Color.Transparent))
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
internal class AndroidShaderEffect(shaderCode: String) : ShaderEffect {
    private val shader = RuntimeShader(shaderCode)

    override val supported: Boolean = true
    override var ready: MutableState<Boolean> = mutableStateOf(false)

    override fun updateUniforms(
        time: Float,
        width: Float,
        height: Float,
    ) {
        shader.setFloatUniform("iResolution", width, height, width / height)
        shader.setFloatUniform("iTime", time)
        ready.value = width > 0 && height > 0
    }

    override fun brush(): Brush = ShaderBrush(shader)
}

actual fun build(shaderCode: String): ShaderEffect =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        AndroidShaderEffect(shaderCode)
    } else {
        NoOpShaderEffect()
    }
