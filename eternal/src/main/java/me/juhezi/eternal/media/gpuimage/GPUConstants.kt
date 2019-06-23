package me.juhezi.eternal.media.gpuimage

val NO_TEXTURE = -1

val BYTES_PER_FLOAT = 4

val BYTES_PER_SHORT = 2

const val POSITION_COMPONENT_COUNT = 2  //  每个顶点有多少个分量

// 默认顶点坐标
val CUBE = floatArrayOf(    // 其实是两个三角形拼起来的。三维坐标系
    -1.0f, -1.0f,
    1.0f, -1.0f,
    -1.0f, 1.0f,
    1.0f, 1.0f
)

// 默认纹理坐标
val ST = floatArrayOf(  // UV 坐标系，也是两个三角形拼起来的
    0.0f, 1.0f,
    1.0f, 1.0f,
    0.0f, 0.0f,
    1.0f, 0.0f
)

const val BASE_VERTEX_SHADER = """
attribute vec4 aPosition;
uniform mat4 uMatrix;

void main() {
    gl_Position = uMatrix * aPosition;
}
"""

const val BASE_FRAGMENT_SHADER = """
void main() {
    gl_FragColor = vec4(1.0f, 1.0f, 0.0f, 1.0f);
}
"""