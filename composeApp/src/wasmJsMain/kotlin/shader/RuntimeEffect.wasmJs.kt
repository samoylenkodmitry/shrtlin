package shader

actual fun build(shaderCode: String): ShaderEffect = NonAndroidShaderEffect(shaderCode)
