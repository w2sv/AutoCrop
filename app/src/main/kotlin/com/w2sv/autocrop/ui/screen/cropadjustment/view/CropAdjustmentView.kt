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

    private lateinit var imageState: ImageState
    private lateinit var imageMatrixController: ImageMatrixController
    private lateinit var cropState: CropState
    private lateinit var mode: CropAdjustmentViewMode

    var imageMatrixChangedListener: ((Matrix) -> Unit)? = null
    var adjustmentModeStateChangedListener: (AdjustmentModeState) -> Unit = {}

    var overlays: Overlays by Delegates.observable(Overlays.CropMask) { _, oldValue, newValue ->
        if (oldValue != newValue) {
            invalidate()
        }
    }

    init {
        scaleType = ScaleType.MATRIX
    }

    fun initialize(image: Bitmap, cropEdges: CropEdges) {
        imageState = ImageState(image)
        setImageBitmap(image)
        imageMatrixController = ImageMatrixController(
            imageMatrix = { imageMatrix },
            setImageMatrix = { imageMatrix = it },
            imageState = imageState
        )
        cropState = CropState(cropEdges, imageState.width, imageMatrixController)
        mode = CropAdjustmentViewManualMode(
            view = this,
            cropState = cropState,
            imageMatrixController = imageMatrixController,
            emitModeState = { adjustmentModeStateChangedListener(it) }
        )
    }

    /**
     * Updates the view to display a crop rect corresponding to the passed [edges] IF [edges] are dissimilar to the currently displayed ones.
     * Enables view updates from domain layer instead of only through user interaction.
     */
    fun updateFromEdges(edges: CropEdges) {
        // Property update flow is inverted -> on gesture interaction cropState.rect & imageMatrix are updated and cropState.edges is derived
        // from them. Now edges are the source of truth and cropRect and imageMatrix have to be updated to match them.
        cropState.updateEdgesIfDissimilar(edges) { mode.displayCropRect(it) }
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
            imageMatrixController.initializeMatrix(width, height)
            cropState.initializeCropRect()
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (overlays == Overlays.HideCropMaskAndDrawCropAreaBlack || event == null) return false
        return mode.onTouchEvent(event)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        when (overlays) {
            Overlays.CropMask -> {
                OverlayRenderer.drawMask(canvas, imageMatrixController.imageRect, cropState.cropRect)
                mode.onDraw(canvas)
            }

            Overlays.HideCropMaskAndDrawCropAreaBlack -> OverlayRenderer.drawCropArea(canvas, cropState.cropRect)
        }
    }

    enum class Overlays {
        CropMask,
        HideCropMaskAndDrawCropAreaBlack
    }

    class ImageMatrixController(
        private val imageMatrix: () -> Matrix,
        private val setImageMatrix: (Matrix) -> Unit,
        private val imageState: ImageState
    ) {
        /**
         * A copy of the initial center fit matrix computed via [createCenterFitMatrix]. Used for returning to the initial view configuration.
         */
        lateinit var centerFitMatrix: Matrix

        /**
         * The [ImageState.rect] in view space.
         */
        val imageRect: RectF
            get() = mapRect(imageState.rect)

        fun initializeMatrix(viewWidth: Int, viewHeight: Int) {
            val matrix = imageState.bitmap.createCenterFitMatrix(viewWidth, viewHeight)
            centerFitMatrix = matrix
            setImageMatrix(matrix)
        }

        fun mapRect(src: RectF): RectF {
            val dst = RectF()
            imageMatrix().mapRect(dst, src)
            return dst
        }

        fun mapRectInverse(src: RectF): RectF =
            mapRect(src, RectF(), imageMatrix().inverse())
    }

    class CropState(private var edges: CropEdges, private val imageWidth: Int, private val matrixController: ImageMatrixController) {
        /**
         * The crop rect in bitmap space derived from [edges]. Used only for initialization of [cropRect]
         */
        private val cropRectBitmapSpace: RectF
            get() = edges.rectF(imageWidth)

        /**
         * The current crop rect in view space.
         * Source of truth for the crop rect to be drawn.
         */
        val cropRect = RectF()

        fun initializeCropRect() {
            cropRect.set(matrixController.mapRect(cropRectBitmapSpace))
        }

        fun updateEdgesIfDissimilar(edges: CropEdges, ifDissimilar: (RectF) -> Unit) {
            if (this.edges != edges) {
                this.edges = edges
                ifDissimilar(cropRectBitmapSpace)
            }
        }

        /**
         * Recomputes and returns the crop edges in bitmap space based on the current matrix.
         */
        fun bitmapSpaceRemappedCropEdges(): CropEdges {
            val cropRectBitmapSpace = matrixController.mapRectInverse(cropRect)
            edges = CropEdges(
                cropRectBitmapSpace.top.roundToInt(),
                cropRectBitmapSpace.bottom.roundToInt()
            )
            return edges
        }
    }

    data class ImageState(var bitmap: Bitmap, val rect: RectF = bitmap.rectF(), val width: Int = bitmap.width)

    private object OverlayRenderer {
        private val maskPaint by threadUnsafeLazyPaint {
            color = ColorUtils.setAlphaComponent(Color.BLACK, 160)
            style = Paint.Style.FILL
        }
        private val blackPaint by threadUnsafeLazyPaint {
            color = Color.BLACK
        }

        fun drawMask(
            canvas: Canvas,
            imageRect: RectF,
            cropRect: RectF
        ) {
            val path = buildPath {
                fillType = Path.FillType.EVEN_ODD
                addRect(imageRect, Path.Direction.CW)
                addRect(cropRect, Path.Direction.CCW)
            }
            canvas.drawPath(path, maskPaint)
        }

        fun drawCropArea(canvas: Canvas, cropRect: RectF) {
            canvas.drawRect(cropRect, blackPaint)
        }
    }

    companion object {

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
