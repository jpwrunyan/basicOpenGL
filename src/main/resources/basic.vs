#version 410

layout (location=0) in vec3 position;
layout (location=1) in vec2 textureCoords;

out vec3 vertColor;
out vec2 fragTextureCoords;

void main() {
    gl_Position = vec4(position, 1.0);
    vertColor = vec3(position.x + 0.5, 1.0, position.y + 0.5);
    fragTextureCoords = textureCoords;
}