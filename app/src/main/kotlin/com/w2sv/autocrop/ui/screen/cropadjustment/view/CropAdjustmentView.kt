package com.w2sv.autocrop.ui.screen.cropadjustment.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.w2sv.autocrop.ui.screen.cropadjustment.extensions.mapRect
import com.w2sv.autocrop.ui.screen.cropadjustment.extensions.rectF
import com.w2sv.autocrop.ui.screen.cropadjustment.model.AdjustmentModeState
import com.w2sv.autocrop.ui.screen.cropadjustment.view.config.CropAdjustmentViewManualMode
import com.w2sv.autocrop.ui.screen.cropadjustment.view.config.CropAdjustmentViewMode
import com.w2sv.autocrop.ui.util.view.buildPath
import com.w2sv.autocrop.ui.util.view.inverse
import com.w2sv.autocrop.ui.util.view.threadUnsafeLazyPaint
import com.w2sv.domain.model.CropAdjustmentMode
import com.w2sv.domain.model.CropEdges
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.properties.Delegates

class CropAdjustmentView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
    View(context, attrs, defStyleAttr) {

    private lateinit var image: Bitmap

    var transformationMatrix: Matrix by Delegates.observable(Matrix()) { _, _, _ -> computeImageRect() }
    lateinit var initialTransformationMatrix: Matrix

    lateinit var imageRectBitmapSpace: RectF
    val imageRect = RectF()

    private lateinit var initialCropRectBitmapSpace: RectF
    val cropRect = RectF()
    lateinit var initialCropRect: RectF

    private var adjustmentModeStateChangedListener: (AdjustmentModeState) -> Unit = {}

    private lateinit var modeConfig: CropAdjustmentViewMode

    fun setAdjustmentModeStateChangedListener(listener: (AdjustmentModeState) -> Unit) {
        adjustmentModeStateChangedListener = listener
    }

    fun initialize(image: Bitmap, cropEdges: CropEdges) {
        this.image = image
        this.imageRectBitmapSpace = image.rectF()
        initialCropRectBitmapSpace = cropEdges.rectF(image.width)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        if (!isInEditMode) {
            setWillNotDraw(false)
        }
    }

    override fun onSizeChanged(
        w: Int,
        h: Int,
        oldw: Int,
        oldh: Int
    ) {
        super.onSizeChanged(w, h, oldw, oldh)

        initialTransformationMatrix = computeImageMatrix()
        transformationMatrix = initialTransformationMatrix

        mapRect(initialCropRectBitmapSpace, cropRect, transformationMatrix)
        initialCropRect = cropRect
    }

    private fun computeImageMatrix(): Matrix =
        Matrix()
            .apply {
                // Scale uniformly to preserve aspect ratio
                val scale = min(width.toFloat() / imageRectBitmapSpace.width(), height.toFloat() / imageRectBitmapSpace.height())
                setScale(scale, scale)

                val dx = (width.toFloat() - imageRectBitmapSpace.width() * scale) / 2f
                val dy = (height.toFloat() - imageRectBitmapSpace.height() * scale) / 2f
                postTranslate(dx, dy)
            }

    fun computeImageRect() {
        mapRect(imageRectBitmapSpace, imageRect, transformationMatrix)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean =
        event
            ?.let { modeConfig.onTouchEvent(it) }
            ?: false

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        canvas.drawBitmap(image, transformationMatrix, null)
        modeConfig.onDraw(canvas)
    }

    fun drawCropMask(canvas: Canvas) {
        val path = buildPath {
            fillType = Path.FillType.EVEN_ODD
            addRect(imageRect, Path.Direction.CW)
            addRect(cropRect, Path.Direction.CCW)
        }
        canvas.drawPath(path, maskPaint)
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

    fun resetCropRect() {
        cropRect.set(initialCropRect)
    }

    fun emitModeState(state: AdjustmentModeState) {
        adjustmentModeStateChangedListener(state)
    }

    fun remappedCropEdges(): CropEdges {
        val cropRectImageDomain = mapRect(cropRect, RectF(), transformationMatrix.inverse())
        return CropEdges(cropRectImageDomain.top.roundToInt(), cropRectImageDomain.bottom.roundToInt())
    }

    companion object {
        private val maskPaint by threadUnsafeLazyPaint {
            color = 2870746142.toInt()
            style = Paint.Style.FILL
        }
    }
}
