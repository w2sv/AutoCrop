package com.w2sv.autocrop.ui.screen.cropadjustment.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.graphics.withSave
import com.w2sv.autocrop.R
import com.w2sv.autocrop.ui.screen.cropadjustment.extensions.mapRect
import com.w2sv.autocrop.ui.screen.cropadjustment.extensions.rectF
import com.w2sv.autocrop.ui.screen.cropadjustment.model.AdjustmentModeState
import com.w2sv.autocrop.ui.screen.cropadjustment.view.config.CropAdjustmentViewManualMode
import com.w2sv.autocrop.ui.screen.cropadjustment.view.config.CropAdjustmentViewMode
import com.w2sv.autocrop.ui.util.view.animateMatrix
import com.w2sv.autocrop.ui.util.view.inverse
import com.w2sv.domain.model.CropAdjustmentMode
import com.w2sv.domain.model.CropEdges
import kotlin.math.min
import kotlin.math.roundToInt

class CropAdjustmentView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private lateinit var image: Bitmap
    lateinit var imageRect: RectF

    private lateinit var initialCropRect: RectF

    lateinit var imageMatrix: Matrix
    lateinit var defaultImageMatrix: Matrix

    val imageBorderRect = RectF()

    fun setAdjustmentModeStateChangedListener(listener: (AdjustmentModeState) -> Unit) {
        adjustmentModeStateChangedListener = listener
    }

    private var adjustmentModeStateChangedListener: (AdjustmentModeState) -> Unit = {}

    val cropRect = RectF()

    lateinit var defaultCropRect: RectF

    private lateinit var modeConfig: CropAdjustmentViewMode

    fun initialize(image: Bitmap, cropEdges: CropEdges) {
        this.image = image
        this.imageRect = image.rectF()
        initialCropRect = cropEdges.rectF(image.width)
    }

    fun emitModeState(state: AdjustmentModeState) {
        adjustmentModeStateChangedListener(state)
    }

    fun setImageBorderRect() {
        mapRect(imageRect, imageBorderRect, imageMatrix)
    }

    fun resetCropRect() {
        cropRect.set(defaultCropRect)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        if (!isInEditMode) {
            setWillNotDraw(false)
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        defaultImageMatrix = computeImageMatrix()
        imageMatrix = defaultImageMatrix

        mapRect(initialCropRect, cropRect, imageMatrix)
        defaultCropRect = cropRect
    }

    private fun computeImageMatrix(): Matrix =
        Matrix()
            .apply {
                // Scale uniformly to preserve aspect ratio
                val scale = min(width.toFloat() / imageRect.width(), height.toFloat() / imageRect.height())
                setScale(scale, scale)

                val dx = (width.toFloat() - imageRect.width() * scale) / 2f
                val dy = (height.toFloat() - imageRect.height() * scale) / 2f
                postTranslate(dx, dy)
            }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean =
        when (event) {
            null -> false
            else -> modeConfig.onTouchEvent(event)
        }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        canvas.drawBitmap(image, imageMatrix, null)
        modeConfig.onDraw(canvas)
    }

    fun drawCropRect(canvas: Canvas) {
        canvas.withSave {
            clipRect(cropRect)
            drawColor(context.getColor(R.color.crop_mask))
        }
    }

    fun setModeConfig(mode: CropAdjustmentMode) {
        //        modeConfig = when (mode) {
        //            CropAdjustmentMode.Manual -> CropAdjustmentViewManualMode(this, context)
        //            CropAdjustmentMode.EdgeSelection -> TODO()
        //        }
        modeConfig = CropAdjustmentViewManualMode(this, context)
        post { modeConfig.setUp() }
    }

    fun reset() {
        modeConfig.reset()
    }

    fun animateImageTo(dst: Matrix, onEnd: (() -> Unit) = {}) {
        animateMatrix(
            src = imageMatrix,
            dst = dst,
            duration = ANIMATION_DURATION,
            onUpdate = { matrix ->
                imageMatrix = matrix
                invalidate()
            },
            onEnd = onEnd
        )
    }

    fun remappedCropEdges(): CropEdges {
        val cropRectImageDomain = mapRect(cropRect, RectF(), imageMatrix.inverse())
        return CropEdges(cropRectImageDomain.top.roundToInt(), cropRectImageDomain.bottom.roundToInt())
    }

    companion object {
        const val TOUCH_TOLERANCE_MARGIN: Float = 32f
        const val ANIMATION_DURATION: Long = 300
    }
}
