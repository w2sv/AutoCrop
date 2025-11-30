package com.w2sv.flowfield.rendering.util

import android.opengl.GLES30

@JvmInline
internal value class GlProgram(val id: Int) {

    fun use() {
        GLES30.glUseProgram(id)
    }

    fun uniformLocation(name: String): Int =
        GLES30.glGetUniformLocation(id, name)

    companion object {
        fun create(vertexCode: String, fragmentCode: String): GlProgram =
            GlProgram(GLES30.glCreateProgram()).apply {
                GLES30.glAttachShader(id, loadShader(GLES30.GL_VERTEX_SHADER, vertexCode))
                GLES30.glAttachShader(id, loadShader(GLES30.GL_FRAGMENT_SHADER, fragmentCode))
                GLES30.glLinkProgram(id)
                val linkStatus = IntArray(1)
                GLES30.glGetProgramiv(id, GLES30.GL_LINK_STATUS, linkStatus, 0)
                if (linkStatus[0] == 0) {
                    val info = GLES30.glGetProgramInfoLog(id)
                    GLES30.glDeleteProgram(id)
                    throw RuntimeException("Program link failed: $info")
                }
            }

        private fun loadShader(type: Int, code: String): Int =
            GLES30.glCreateShader(type).also { shader ->
                GLES30.glShaderSource(shader, code)
                GLES30.glCompileShader(shader)
                val compiled = IntArray(1)
                GLES30.glGetShaderiv(shader, GLES30.GL_COMPILE_STATUS, compiled, 0)
                if (compiled[0] == 0) {
                    val info = GLES30.glGetShaderInfoLog(shader)
                    GLES30.glDeleteShader(shader)
                    throw RuntimeException("Shader compilation failed: $info")
                }
            }
    }
}
