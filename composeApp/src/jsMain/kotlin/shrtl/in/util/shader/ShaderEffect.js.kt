package shrtl.`in`.util.shader

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.ShaderBrush
import org.jetbrains.skia.RuntimeEffect
import org.jetbrains.skia.RuntimeShaderBuilder

actual fun build(shaderCode: String): ShaderEffect = NonAndroidShaderEffect(shaderCode)

class NonAndroidShaderEffect(shaderCode: String) : CommonNonAndroidShaderEffect(shaderCode) {
    private val shaderBuilder = RuntimeShaderBuilder(RuntimeEffect.makeForShader(shaderCode))

    override fun setUniforms(
        time: Float,
        width: Float,
        height: Float,
    ) {
        shaderBuilder.uniform("iResolution", width, height, width / height)
        shaderBuilder.uniform("iTime", time)
    }

    override fun brush(): Brush = ShaderBrush(shaderBuilder.makeShader())
}
