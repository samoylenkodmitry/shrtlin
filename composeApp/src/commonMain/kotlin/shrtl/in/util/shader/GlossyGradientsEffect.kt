package shrtl.`in`.util.shader

/**
 *
 * LICENSE "Creative Commons Attribution-NonCommercial-ShareAlike 3.0 Unported License"
 * Author: https://www.shadertoy.com/user/Peace
 * Credit: https://www.shadertoy.com/view/lX2GDR
 */
const val GLOSSY_GRADIENTS_EFFECT = """
uniform vec3      iResolution;           // viewport resolution (in pixels)
uniform float     iTime;                 // shader playback time (in seconds)
    vec4 main( in vec2 fragCoord )
{
    float mr = min(iResolution.x, iResolution.y);
    vec2 uv = (fragCoord * 2.0 - iResolution.xy) / mr;

    float d = -iTime * 0.05;
    float a = 0.0;
    for (float i = 0.0; i < 8.0; ++i) {
        a += cos(i - d - a * uv.x);
        d += sin(uv.y * i + a);
    }
    d += iTime * 0.05;
    vec3 col = vec3(cos(uv * vec2(d, a)) * 0.6 + 0.4, cos(a + d) * 0.5 + 0.5);
    col = cos(col * cos(vec3(d, a, 2.5)) * 0.5 + 0.5);
    return vec4(col,1.0);   
}
"""
