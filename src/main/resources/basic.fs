#version 410

in vec3 vertColor;
in vec2 fragTextureCoords;

out vec4 fragColor;

uniform sampler2D textureSampler;

void main() {
    //fragColor = vec4(0.0, 0.5, 0.5, 1.0);
    //fragColor = vec4(vertColor, 1.0);
    fragColor = texture(textureSampler, fragTextureCoords);
}