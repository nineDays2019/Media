package me.juhezi.eternal.gpuimage

class FragmentShaderBuilder {

    companion object {
        val DEFAULT_DEFINES = """
#ifdef GL_ES
precision mediump float;
#endif

#define PI 3.14159265359""".trimIndent()
        val DEFAULT_ATTRIBUTES = """
uniform vec2 u_resolution;
uniform vec2 u_mouse;
uniform float u_time;
        """.trimIndent()
    }

    var defines: MutableList<String> = mutableListOf() // 宏定义

    var attributes: MutableList<String> = mutableListOf()  // 属性（多个）定义

    var main: String = ""   // 主函数定义

    var functions: MutableList<String> = mutableListOf()  // 子函数（多个）定义

    fun define(content: String) {
        defines.add(content)
    }

    fun attribute(content: String) {
        attributes.add(content)
    }

    fun function(content: String) {
        functions.add(content)
    }

    fun main(content: String) {
        main = """
            void main() {
                $content
            }
        """.trimIndent()
    }

    fun build() = buildString {
        defines.forEach {
            append("$it\n")
        }
        attributes.forEach {
            append("$it\n")
        }
        functions.forEach {
            append("$it\n")
        }
        append("$main\n")
    }

    // 具体的函数
    fun plotFunction() = """
        float plot (vec2 st, float pct){
            return smoothstep( pct-0.01, pct, st.y) -
                smoothstep( pct, pct+0.01, st.y);
        }
    """.trimIndent()

}

fun buildFragmentShader(closure: FragmentShaderBuilder.() -> Unit): String {
    val builder = FragmentShaderBuilder()
    closure(builder)
    return builder.build()
}

fun buildSpecialFragmentShader(closure: FragmentShaderBuilder.() -> Unit): String {
    val builder = FragmentShaderBuilder()
    builder.define(FragmentShaderBuilder.DEFAULT_DEFINES)
    builder.attribute(FragmentShaderBuilder.DEFAULT_ATTRIBUTES)
    closure(builder)
    return builder.build()
}