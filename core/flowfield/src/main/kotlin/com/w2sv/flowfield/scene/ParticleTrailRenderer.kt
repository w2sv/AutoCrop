package com.w2sv.flowfield.scene

import android.content.res.AssetManager
import com.google.android.filament.Box
import com.google.android.filament.Engine
import com.google.android.filament.EntityManager
import com.google.android.filament.IndexBuffer
import com.google.android.filament.Material
import com.google.android.filament.MaterialInstance
import com.google.android.filament.RenderableManager
import com.google.android.filament.Scene
import com.google.android.filament.VertexBuffer
import com.w2sv.flowfield.simulation.Particle
import com.w2sv.flowfield.simulation.Vec2
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.abs

class ParticleTrailRenderer(private val engine: Engine, private val scene: Scene, assetManager: AssetManager) {
    private data class ParticleRenderData(
        val particle: Particle,
        val entity: Int,
        val vertexBuffer: VertexBuffer,
        val indexBuffer: IndexBuffer,
        val materialInstance: MaterialInstance,
        val trailPositions: ArrayDeque<Vec2> = ArrayDeque(MAX_TRAIL_LENGTH),
        var currentHue: Float = 0f
    )

    companion object {
        private const val MAX_TRAIL_LENGTH = 50
        private const val COLOR_CHANGE_SPEED = 0.1f
    }

    private val particleRenderData = mutableListOf<ParticleRenderData>()
    private var viewWidth = 1
    private var viewHeight = 1
    private val unlitMaterial: Material

    init {
        unlitMaterial = createUnlitMaterial(assetManager)
    }

    fun initializeParticles(particles: List<Particle>) {
        particles.forEach { particle ->
            val renderData = createTrailRenderData(particle)
            particleRenderData.add(renderData)
        }
    }

    private fun createTrailRenderData(particle: Particle): ParticleRenderData {
        val entity = EntityManager.get().create()

        // Create vertex buffer
        val vertexBuffer = VertexBuffer.Builder()
            .vertexCount(MAX_TRAIL_LENGTH)
            .bufferCount(2)
            .attribute(VertexBuffer.VertexAttribute.POSITION, 0, VertexBuffer.AttributeType.FLOAT3, 0, 12)
            .attribute(VertexBuffer.VertexAttribute.COLOR, 1, VertexBuffer.AttributeType.FLOAT4, 0, 16)
            .build(engine)

        // Create index buffer
        val indexBuffer = createLineStripIndexBuffer()

        // Create material instance
        val materialInstance = unlitMaterial.createInstance()

        // Build renderable
        RenderableManager.Builder(1)
            .boundingBox(Box(-1f, -1f, -1f, 1f, 1f, 1f))
            .geometry(0, RenderableManager.PrimitiveType.LINES, vertexBuffer, indexBuffer, 0, 0)
            .material(0, materialInstance)
            .build(engine, entity)

        scene.addEntity(entity)

        return ParticleRenderData(
            particle = particle,
            entity = entity,
            vertexBuffer = vertexBuffer,
            indexBuffer = indexBuffer,
            materialInstance = materialInstance
        )
    }

    private fun createLineStripIndexBuffer(): IndexBuffer {
        val indices = ShortArray((MAX_TRAIL_LENGTH - 1) * 2)
        for (i in 0 until MAX_TRAIL_LENGTH - 1) {
            indices[i * 2] = i.toShort()
            indices[i * 2 + 1] = (i + 1).toShort()
        }

        val indexBuffer = IndexBuffer.Builder()
            .indexCount(indices.size)
            .bufferType(IndexBuffer.Builder.IndexType.USHORT)
            .build(engine)

        indexBuffer.setBuffer(
            engine,
            ByteBuffer.allocateDirect(indices.size * 2).apply {
                asShortBuffer().put(indices)
            }
        )

        return indexBuffer
    }

    private fun createUnlitMaterial(assetManager: AssetManager): Material {
        // 1. Read the material file from assets into a ByteBuffer
        val fileName = "materials/unlit.filamat" // Adjust the path if needed
        val inputStream = assetManager.open(fileName)
        val fileBytes = inputStream.readBytes()
        inputStream.close()

        val buffer = ByteBuffer.allocateDirect(fileBytes.size)
        buffer.order(ByteOrder.nativeOrder())
        buffer.put(fileBytes)
        buffer.rewind() // Reset position to the start of the buffer

        // 2. Build the material using the buffer
        return Material.Builder()
            .payload(buffer, buffer.remaining())
            .build(engine)
    }

    fun update(deltaTime: Float) {
        updateTrailColors(deltaTime)
        updateTrailGeometry()
    }

    private fun updateTrailColors(deltaTime: Float) {
        particleRenderData.forEach { renderData ->
            renderData.currentHue = (renderData.currentHue + COLOR_CHANGE_SPEED * deltaTime) % 360f
        }
    }

    private fun updateTrailGeometry() {
        particleRenderData.forEach { renderData ->
            val particle = renderData.particle

            if (!particle.shouldSkipDraw()) {
                renderData.trailPositions.addFirst(Vec2(particle.pos.x, particle.pos.y))
                if (renderData.trailPositions.size > MAX_TRAIL_LENGTH) {
                    renderData.trailPositions.removeLast()
                }
            }
            else {
                renderData.trailPositions.clear()
            }

            updateVertexBuffers(renderData)
            updateRenderableGeometry(renderData)
        }
    }

    private fun updateVertexBuffers(renderData: ParticleRenderData) {
        val trailSize = renderData.trailPositions.size
        if (trailSize == 0) return

        val positions = FloatArray(trailSize * 3)
        val colors = FloatArray(trailSize * 4)

        renderData.trailPositions.forEachIndexed { index, pos ->
            val x = (pos.x / viewWidth) * 2 - 1
            val y = 1 - (pos.y / viewHeight) * 2

            positions[index * 3] = x
            positions[index * 3 + 1] = y
            positions[index * 3 + 2] = 0f

            val alpha = 1f - (index.toFloat() / trailSize)
            val rgb = hslToRgb(renderData.currentHue, 1f, 0.7f)

            colors[index * 4] = rgb.r
            colors[index * 4 + 1] = rgb.g
            colors[index * 4 + 2] = rgb.b
            colors[index * 4 + 3] = alpha
        }

        renderData.vertexBuffer.setBufferAt(
            engine, 0,
            ByteBuffer.allocateDirect(positions.size * 4).apply {
                asFloatBuffer().put(positions)
            }
        )

        renderData.vertexBuffer.setBufferAt(
            engine, 1,
            ByteBuffer.allocateDirect(colors.size * 4).apply {
                asFloatBuffer().put(colors)
            }
        )
    }

    private fun updateRenderableGeometry(renderData: ParticleRenderData) {
        val renderableManager = engine.renderableManager
        val instance = renderableManager.getInstance(renderData.entity)
        if (instance == 0) return

        val trailSize = renderData.trailPositions.size
        val indexCount = if (trailSize > 1) (trailSize - 1) * 2 else 0

        renderableManager.setGeometryAt(
            instance,
            0,
            RenderableManager.PrimitiveType.LINES,
            renderData.vertexBuffer,
            renderData.indexBuffer,
            0,
            indexCount
        )
    }

    fun setViewSize(width: Int, height: Int) {
        viewWidth = width
        viewHeight = height
    }

    fun destroy() {
        particleRenderData.forEach { renderData ->
            scene.removeEntity(renderData.entity)
            engine.destroyMaterialInstance(renderData.materialInstance)
            engine.destroyVertexBuffer(renderData.vertexBuffer)
            engine.destroyIndexBuffer(renderData.indexBuffer)
            engine.destroyEntity(renderData.entity)
        }
        particleRenderData.clear()
        engine.destroyMaterial(unlitMaterial)
    }

    private data class Color(val r: Float, val g: Float, val b: Float, val a: Float = 1f)

    private fun hslToRgb(h: Float, s: Float, l: Float): Color {
        val normalizedH = h % 360f
        val c = (1 - abs(2 * l - 1)) * s
        val x = c * (1 - abs((normalizedH / 60) % 2 - 1))
        val m = l - c / 2

        val (r, g, b) = when {
            normalizedH < 60 -> Triple(c, x, 0f)
            normalizedH < 120 -> Triple(x, c, 0f)
            normalizedH < 180 -> Triple(0f, c, x)
            normalizedH < 240 -> Triple(0f, x, c)
            normalizedH < 300 -> Triple(x, 0f, c)
            else -> Triple(c, 0f, x)
        }

        return Color(r + m, g + m, b + m)
    }
}
