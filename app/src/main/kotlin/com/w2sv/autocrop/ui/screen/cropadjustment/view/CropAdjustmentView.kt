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
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.TouchDelegate
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.graphics.ColorUtils
import com.w2sv.autocrop.ui.screen.cropadjustment.extensions.rectF
import com.w2sv.autocrop.ui.screen.cropadjustment.model.AdjustmentModeState
import com.w2sv.autocrop.ui.screen.cropadjustment.view.config.CropAdjustmentViewManualMode
import com.w2sv.autocrop.ui.screen.cropadjustment.view.config.CropAdjustmentViewMode
import com.w2sv.autocrop.ui.util.view.buildPath
import com.w2sv.autocrop.ui.util.view.inverse
import com.w2sv.autocrop.ui.util.view.mappedRect
import com.w2sv.autocrop.ui.util.view.threadUnsafeLazyPaint
import com.w2sv.domain.model.CropEdges
import kotlin.math.min
import kotlin.properties.Delegates

class CropAdjustmentView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
    AppCompatImageView(context, attrs, defStyleAttr) {

    private lateinit var imageState: ImageState
    private lateinit var coordinateMapper: ImageCoordinateMapper
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
        coordinateMapper = ImageCoordinateMapper(
            imageMatrix = { imageMatrix },
            setImageMatrix = { imageMatrix = it },
            imageState = imageState
        )
        cropState = CropState(cropEdges, imageState.width, coordinateMapper)
        mode = CropAdjustmentViewManualMode(
            view = this,
            cropState = cropState,
            coordinateMapper = coordinateMapper,
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
            coordinateMapper.initializeMatrix(width, height)
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
                OverlayRenderer.drawMask(canvas, coordinateMapper.imageRect, cropState.cropRect)
                mode.onDraw(canvas)
            }

            Overlays.HideCropMaskAndDrawCropAreaBlack -> OverlayRenderer.drawCropArea(canvas, cropState.cropRect)
        }
    }

    enum class Overlays {
        CropMask,
        HideCropMaskAndDrawCropAreaBlack
    }

    class ImageCoordinateMapper(
        private val imageMatrix: () -> Matrix,
        private val setImageMatrix: (Matrix) -> Unit,
        private val imageState: ImageState
    ) {
        /**
         * A copy of the initial center fit matrix computed via [computeCenterFitMatrix]. Used for returning to the initial view configuration.
         */
        lateinit var centerFitMatrix: Matrix
            private set

        /**
         * The [ImageState.rect] in view space.
         */
        val imageRect: ViewSpaceRect
            get() = mapToViewSpace(imageState.rect)

        fun initializeMatrix(viewWidth: Int, viewHeight: Int) {
            val matrix = imageState.bitmap.computeCenterFitMatrix(viewWidth, viewHeight)
            centerFitMatrix = matrix
            setImageMatrix(matrix)
        }

        fun mapToViewSpace(src: BitmapSpaceRect): ViewSpaceRect =
            imageMatrix().mappedRect(src).viewSpace

        fun mapToBitmapSpace(src: ViewSpaceRect): BitmapSpaceRect =
            imageMatrix().inverse().mappedRect(src).bitmapSpace
    }

    class CropState(private var edges: CropEdges, private val imageWidth: Int, private val matrixController: ImageCoordinateMapper) {
        /**
         * The crop rect in bitmap space derived from [edges].
         */
        private val cropRectBitmapSpace: BitmapSpaceRect
            get() = edges.rectF(imageWidth).bitmapSpace

        /**
         * The current crop rect in view space.
         * Source of truth for the crop rect to be drawn.
         */
        val cropRect = ViewSpaceRect()

        /**
         * Initializes [cropRect] based on [matrixController].
         */
        fun initializeCropRect() {
            cropRect.set(matrixController.mapToViewSpace(cropRectBitmapSpace))
        }

        fun updateEdgesIfDissimilar(edges: CropEdges, ifDissimilar: (BitmapSpaceRect) -> Unit) {
            if (this.edges != edges) {
                this.edges = edges
                ifDissimilar(cropRectBitmapSpace)
            }
        }

        /**
         * Recomputes and returns the crop edges in bitmap space based on [cropRect] and the current state of [matrixController].
         */
        fun remappedCropEdges(): CropEdges {
            edges = matrixController.mapToBitmapSpace(cropRect).cropEdges()
            return edges
        }
    }

    data class ImageState(val bitmap: Bitmap, val rect: BitmapSpaceRect = bitmap.rectF().bitmapSpace, val width: Int = bitmap.width)

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
            imageRect: ViewSpaceRect,
            cropRect: ViewSpaceRect
        ) {
            val path = buildPath {
                fillType = Path.FillType.EVEN_ODD
                addRect(imageRect, Path.Direction.CW)
                addRect(cropRect, Path.Direction.CCW)
            }
            canvas.drawPath(path, maskPaint)
        }

        fun drawCropArea(canvas: Canvas, cropRect: ViewSpaceRect) {
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
 * Computes a [Matrix] that scales and centers this bitmap to fit within the given view dimensions
 * while preserving aspect ratio.
 *
 * @param viewWidth the width of the view to fit into
 * @param viewHeight the height of the view to fit into
 * @return a new [Matrix] configured for center-fit transformation
 */
private fun Bitmap.computeCenterFitMatrix(viewWidth: Int, viewHeight: Int): Matrix =
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
