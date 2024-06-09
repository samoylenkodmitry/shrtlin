package shrtl.`in`.util.shader

/**
 *
 * LICENSE "Creative Commons Attribution-NonCommercial-ShareAlike 3.0 Unported License"
 * Author: https://www.shadertoy.com/user/TAKUSAKU
 * Credit: https://www.shadertoy.com/view/WdjBWD
 */
const val INK_FLOW_EFFECT = """
uniform vec3      iResolution;           // viewport resolution (in pixels)
uniform float     iTime;                 // shader playback time (in seconds)
    vec4 main( in vec2 fragCoord )
{
    vec2 st = (fragCoord.xy * 2. - iResolution.xy) / min(iResolution.x, iResolution.y);
    
    st *= 2.5;

    vec2 coord = st;
    float len;
    for (int i = 0; i < 3; i++) {
        len = length(coord);
        coord.x +=  sin(coord.y + iTime * 0.3)*1.;
        coord.y +=  cos(coord.x + iTime * 0.1 + cos(len * 1.0))*6.;
    }
         
    vec3 col = vec3(0.);

    col = mix(col, vec3(cos(len)), 1.0);
    
    return vec4(0.7*col,1.);      
}
"""
