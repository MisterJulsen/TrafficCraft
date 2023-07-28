#version 150

#moj_import <fog.glsl>

uniform sampler2D Sampler0;

uniform vec4 ColorModulator;
uniform float FogStart;
uniform float FogEnd;
uniform vec4 FogColor;

in float vertexDistance;
in vec4 vertexColor;
in vec2 texCoord0;
in vec4 normal;

out vec4 fragColor;

void main() {
    vec4 tex = texture(Sampler0, texCoord0);
    vec4 color;
    if (tex.a == (254.0 / 255.0)){
        tex.a = 1.0;
        color = tex * ColorModulator;
        color.rgb *= 2.0; // erhöht die Farbintensität
        fragColor = linear_fog(color, vertexDistance, FogStart, FogEnd * 0.5, FogColor); // verringert die Entfernung, bei der der Leucht-Effekt endet
    }else{
        color = texture(Sampler0, texCoord0) * vertexColor * ColorModulator;
        fragColor = linear_fog(color, vertexDistance, FogStart, FogEnd, FogColor);
    }
}
