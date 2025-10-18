package com.w2sv.autocrop.ui.screen.cropadjustment.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.TouchDelegate
import android.view.View
import android.view.ViewGroup
import com.w2sv.autocrop.ui.screen.cropadjustment.extensions.mapRect
import com.w2sv.autocrop.ui.screen.cropadjustment.extensions.rectF
import com.w2sv.autocrop.ui.screen.cropadjustment.model.AdjustmentModeState
import com.w2sv.autocrop.ui.screen.cropadjustment.view.config.CropAdjustmentViewManualMode
import com.w2sv.autocrop.ui.screen.cropadjustment.view.config.CropAdjustmentViewMode
import com.w2sv.autocrop.ui.util.view.buildPath
import com.w2sv.autocrop.ui.util.view.inverse
import com.w2sv.autocrop.ui.util.view.threadUnsafeLazyPaint
import com.w2sv.domain.model.CropEdges
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.properties.Delegates
import slimber.log.i

class CropAdjustmentView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
    View(context, attrs, defStyleAttr) {

    private lateinit var image: Bitmap

    var transformationMatrix: Matrix by Delegates.observable(Matrix()) { _, _, _ ->
        // Compute imageRect
        mapRect(imageRectBitmapSpace, imageRect, transformationMatrix)
    }
    lateinit var defaultTransformationMatrix: Matrix

    lateinit var imageRectBitmapSpace: RectF
    val imageRect = RectF()

    private lateinit var cropEdges: CropEdges
    val cropEdgesRect: RectF get() = cropEdges.rectF(image.width)
    val cropRect = RectF()

    var adjustmentModeStateChangedListener: (AdjustmentModeState) -> Unit = {}

    private val modeConfig: CropAdjustmentViewMode = CropAdjustmentViewManualMode(this, context)

    fun initialize(image: Bitmap, cropEdges: CropEdges) {
        this.image = image
        this.imageRectBitmapSpace = image.rectF()
        this.cropEdges = cropEdges

        initializeMatrix()
    }

    fun updateFromEdges(edges: CropEdges) {
        if (edges != cropEdges) {
            i { "Updating from edges" }
            cropEdges = edges
            modeConfig.updateFromEdges(edges)
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        if (!isInEditMode) {
            setWillNotDraw(false)
            expandVerticalTouchArea(EDGE_TOUCH_SLOP)
        }
    }

    override fun onSizeChanged(
        w: Int,
        h: Int,
        oldw: Int,
        oldh: Int
    ) {
        initializeMatrix()
    }

    private fun initializeMatrix() {
        defaultTransformationMatrix = imageRectBitmapSpace.centeringMatrixAcross(width.toFloat(), height.toFloat())
        transformationMatrix = defaultTransformationMatrix

        // Set cropRect
        mapRect(cropEdgesRect, cropRect, transformationMatrix)
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

    fun emitModeState(state: AdjustmentModeState) {
        adjustmentModeStateChangedListener(state)
    }

    fun remappedCropEdges(): CropEdges {
        val cropRectImageDomain = mapRect(cropRect, RectF(), transformationMatrix.inverse())
        cropEdges = CropEdges(cropRectImageDomain.top.roundToInt(), cropRectImageDomain.bottom.roundToInt())
        return cropEdges
    }

    companion object {
        private val maskPaint by threadUnsafeLazyPaint {
            color = 2870746142.toInt()
            style = Paint.Style.FILL
        }

        /**
         * The vertical distance in pixels within which a touch is still considered
         * to be targeting a crop edge, even if it doesn't land exactly on it.
         *
         * Used to make edge dragging easier when the user's finger is slightly
         * above or below the visible edge.
         */
        const val EDGE_TOUCH_SLOP = 42
    }
}

private fun RectF.centeringMatrixAcross(referenceWidth: Float, referenceHeight: Float): Matrix =
    Matrix().apply {
        // Scale uniformly to preserve aspect ratio
        val scale = min(referenceWidth / width(), referenceHeight / height())
        postScale(scale, scale)

        val dx = (referenceWidth - width() * scale) / 2f
        val dy = (referenceHeight - height() * scale) / 2f
        postTranslate(dx, dy)
    }

private fun View.expandVerticalTouchArea(px: Int) {
    val parentView = parent as? ViewGroup
        ?: return
    parentView.post {
        val rect = Rect()
        getHitRect(rect)
        rect.top -= px
        rect.bottom += px
        parentView.touchDelegate = TouchDelegate(rect, this)
    }
}
