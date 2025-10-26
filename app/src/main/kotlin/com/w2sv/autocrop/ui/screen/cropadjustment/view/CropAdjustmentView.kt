package com.w2sv.autocrop.ui.screen.cropadjustment.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
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
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.graphics.ColorUtils
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

class CropAdjustmentView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
    AppCompatImageView(context, attrs, defStyleAttr) {

    lateinit var image: Bitmap

    lateinit var initialImageMatrx: Matrix
    var imageMatrixChangedListener: ((Matrix) -> Unit)? = null

    lateinit var imageRectBitmapSpace: RectF
    val imageRect get() = mapRect(imageRectBitmapSpace, matrix = imageMatrix)

    lateinit var cropEdgesBitmapSpace: CropEdges
    val cropRectBitmapSpace: RectF get() = cropEdgesBitmapSpace.rectF(image.width)
    val cropRect = RectF()

    var adjustmentModeStateChangedListener: (AdjustmentModeState) -> Unit = {}

    private val modeConfig: CropAdjustmentViewMode = CropAdjustmentViewManualMode(this, context)

    var overlays: Overlays by Delegates.observable(Overlays.CropMask) { _, oldValue, newValue ->
        if (oldValue != newValue) {
            invalidate()
        }
    }

    init {
        scaleType = ScaleType.MATRIX
    }

    fun initialize(image: Bitmap, cropEdges: CropEdges) {
        this.image = image
        setImageBitmap(image)
        this.imageRectBitmapSpace = image.rectF()
        this.cropEdgesBitmapSpace = cropEdges
    }

    fun updateFromEdges(edges: CropEdges) {
        if (edges != cropEdgesBitmapSpace) {
            modeConfig.updateFromEdges(edges)
        }
    }

    override fun setImageMatrix(matrix: Matrix?) {
        super.setImageMatrix(matrix)
        if (matrix != null) {
            imageMatrixChangedListener?.invoke(matrix)
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        expandVerticalTouchArea(VERTICAL_EDGE_TOUCH_SLOP)
    }

    override fun onLayout(
        changed: Boolean,
        left: Int,
        top: Int,
        right: Int,
        bottom: Int
    ) {
        super.onLayout(changed, left, top, right, bottom)
        if (changed) {
            // Initialize imageMatrix
            val matrix = image.createCenterFitMatrix(width, height)
            imageMatrix = matrix
            initialImageMatrx = matrix

            // Initialize the cropRect with the now computed matrix
            mapRect(cropRectBitmapSpace, cropRect, matrix)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (overlays == Overlays.HideCropMaskAndDrawCropAreaBlack || event == null) return false
        return modeConfig.onTouchEvent(event)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        when (overlays) {
            Overlays.CropMask -> modeConfig.onDraw(canvas)
            Overlays.HideCropMaskAndDrawCropAreaBlack -> canvas.drawRect(cropRect, blackPaint)
        }
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
        val cropRectImageDomain = mapRect(cropRect, RectF(), imageMatrix.inverse())
        cropEdgesBitmapSpace = CropEdges(cropRectImageDomain.top.roundToInt(), cropRectImageDomain.bottom.roundToInt())
        return cropEdgesBitmapSpace
    }

    enum class Overlays {
        CropMask,
        HideCropMaskAndDrawCropAreaBlack
    }

    companion object {
        private val maskPaint by threadUnsafeLazyPaint {
            color = ColorUtils.setAlphaComponent(Color.BLACK, 160)
            style = Paint.Style.FILL
        }
        private val blackPaint by threadUnsafeLazyPaint {
            color = Color.BLACK
        }

        /**
         * The vertical distance in pixels within which a touch is still considered
         * to be targeting a crop edge, even if it doesn't land exactly on it.
         *
         * Used to make edge dragging easier when the user's finger is slightly
         * above or below the visible edge.
         */
        const val VERTICAL_EDGE_TOUCH_SLOP = 42
    }
}

/**
 * Creates a [Matrix] that scales and centers this bitmap to fit within the given view dimensions
 * while preserving aspect ratio.
 *
 * @param viewWidth the width of the view to fit into
 * @param viewHeight the height of the view to fit into
 * @return a new [Matrix] configured for center-fit transformation
 */
private fun Bitmap.createCenterFitMatrix(viewWidth: Int, viewHeight: Int): Matrix =
    Matrix().apply {
        val scale = min(
            viewWidth / width.toFloat(),
            viewHeight / height.toFloat()
        )
        setScale(scale, scale)
        postTranslate(
            (viewWidth - width * scale) * 0.5f,
            (viewHeight - height * scale) * 0.5f
        )
    }

private fun View.expandVerticalTouchArea(px: Int) {
    (parent as? ViewGroup)?.let { parentView ->
        parentView.post {
            val rect = Rect()
            getHitRect(rect)
            rect.top -= px
            rect.bottom += px
            parentView.touchDelegate = TouchDelegate(rect, this)
        }
    }
}
