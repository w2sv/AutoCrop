package com.w2sv.flowfield.rendering

import android.opengl.GLES30

internal fun createProgram(vertexCode: String, fragmentCode: String): Int {
    val vertex = loadShader(GLES30.GL_VERTEX_SHADER, vertexCode)
    val fragment = loadShader(GLES30.GL_FRAGMENT_SHADER, fragmentCode)
    return GLES30.glCreateProgram().also { program ->
        GLES30.glAttachShader(program, vertex)
        GLES30.glAttachShader(program, fragment)
        GLES30.glLinkProgram(program)
        val linkStatus = IntArray(1)
        GLES30.glGetProgramiv(program, GLES30.GL_LINK_STATUS, linkStatus, 0)
        if (linkStatus[0] == 0) {
            val info = GLES30.glGetProgramInfoLog(program)
            GLES30.glDeleteProgram(program)
            throw RuntimeException("Program link failed: $info")
        }
    }
}

private fun loadShader(type: Int, code: String): Int {
    return GLES30.glCreateShader(type).also { shader ->
        GLES30.glShaderSource(shader, code)
        GLES30.glCompileShader(shader)
        val compiled = IntArray(1)
        GLES30.glGetShaderiv(shader, GLES30.GL_COMPILE_STATUS, compiled, 0)
        if (compiled[0] == 0) {
            val info = GLES30.glGetShaderInfoLog(shader)
            GLES30.glDeleteShader(shader)
            throw RuntimeException("Shader compile failed: $info")
        }
    }
}
