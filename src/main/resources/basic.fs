#version 330

in vec3 vertColor;
out vec4 fragColor;

void main() {
    //fragColor = vec4(0.0, 0.5, 0.5, 1.0);
    fragColor = vec4(vertColor, 1.0);
}