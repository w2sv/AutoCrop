package com.w2sv.flowfield.rendering

import android.opengl.GLES30
import com.w2sv.flowfield.Config
import com.w2sv.flowfield.rendering.util.GlProgram
import com.w2sv.flowfield.rendering.util.wrapper.ArrayBuffer
import com.w2sv.flowfield.rendering.util.wrapper.FrameBuffer
import com.w2sv.flowfield.rendering.util.wrapper.Texture2D
import com.w2sv.flowfield.rendering.util.wrapper.VertexArray
import java.nio.ByteBuffer
import java.nio.ByteOrder

private val vertexShaderCode = """
#version 300 es
layout(location = 0) in vec2 aPos;
layout(location = 1) in vec2 aTex;
out vec2 vTex;
void main() { vTex = aTex; gl_Position = vec4(aPos, 0.0, 1.0); }
""".trimIndent()
private val fragmentShaderCode = """
#version 300 es
precision mediump float;
in vec2 vTex;
uniform sampler2D uTexture;
out vec4 FragColor;
void main() { FragColor = texture(uTexture, vTex); }
""".trimIndent()
private val fadeFragmentShaderCode = """
#version 300 es
precision mediump float;
uniform float uAlpha;
uniform sampler2D uTexture;
in vec2 vTex;
out vec4 FragColor;

void main() {
    vec4 color = texture(uTexture, vTex);
    // Subtract from alpha only - preserves RGB intensity
    color.a = max(0.0, color.a - uAlpha);

    // Cheap cleanup: if alpha is very small, multiply to zero
    color.a *= step(0.005, color.a); // step() is branchless and fast

    FragColor = color;
}
""".trimIndent()


internal class QuadRenderer {
    private val drawProgram = GlProgram.create(vertexShaderCode, fragmentShaderCode)
    private val fadeProgram = GlProgram.create(vertexShaderCode, fadeFragmentShaderCode)

    private val vao = VertexArray()
    private val vbo = ArrayBuffer()
    private val vertices = floatArrayOf(
        -1f, -1f, 0f, 0f,
        1f, -1f, 1f, 0f,
        -1f, 1f, 0f, 1f,
        1f, 1f, 1f, 1f
    )

    init {
        vao.whilstBound {
            vbo.bind()
            val quadBuffer = ByteBuffer.allocateDirect(vertices.size * 4).order(ByteOrder.nativeOrder()).asFloatBuffer()
            quadBuffer.put(vertices).position(0)
            GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER, vertices.size * 4, quadBuffer, GLES30.GL_STATIC_DRAW)
            GLES30.glVertexAttribPointer(0, 2, GLES30.GL_FLOAT, false, 4 * 4, 0)
            GLES30.glEnableVertexAttribArray(0)
            GLES30.glVertexAttribPointer(1, 2, GLES30.GL_FLOAT, false, 4 * 4, 2 * 4)
            GLES30.glEnableVertexAttribArray(1)
        }
    }

    fun draw(texture: Texture2D) {
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT)
        drawProgram.use()
        vao.whilstBound {
            texture.bindToUnit0()

            GLES30.glUniform1i(drawProgram.uniformLocation("uTexture"), 0)
            GLES30.glDrawArrays(GLES30.GL_TRIANGLE_STRIP, 0, 4)
        }
    }

    fun fade(frameBuffer: FrameBuffer, texture: Texture2D) {
        fadeProgram.use()

        frameBuffer.whilstBound {
            vao.whilstBound {
                texture.bindToUnit0()

                // Push shader inputs
                GLES30.glUniform1i(fadeProgram.uniformLocation("uTexture"), 0)
                GLES30.glUniform1f(fadeProgram.uniformLocation("uAlpha"), Config.ALPHA_SUBTRACTION_PER_FRAME)

                // Important: Replace the content, don't blend
                GLES30.glDisable(GLES30.GL_BLEND)
                GLES30.glDrawArrays(GLES30.GL_TRIANGLE_STRIP, 0, 4)
            }
        }
    }
}
