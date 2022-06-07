package com.lyrebirdstudio.croppylib.fragment.cropview

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.MotionEvent.*
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.graphics.toRect
import androidx.core.graphics.toRectF
import com.lyrebirdstudio.croppylib.R
import com.lyrebirdstudio.croppylib.CroppyTheme
import com.lyrebirdstudio.croppylib.utils.extensions.*
import com.lyrebirdstudio.croppylib.utils.model.AnimatableRectF
import com.lyrebirdstudio.croppylib.utils.model.Corner
import com.lyrebirdstudio.croppylib.utils.model.Corner.*
import com.lyrebirdstudio.croppylib.utils.model.DraggingState
import com.lyrebirdstudio.croppylib.utils.model.DraggingState.DraggingCorner
import com.lyrebirdstudio.croppylib.utils.model.DraggingState.DraggingEdge
import com.lyrebirdstudio.croppylib.utils.model.Edge
import com.lyrebirdstudio.croppylib.utils.model.Edge.*
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

class CropView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    var onInitialized: (() -> Unit)? = null

    var observeCropRectOnOriginalBitmapChanged: ((RectF) -> Unit)? = null

    private val cropRectOnOriginalBitmapMatrix = Matrix()

    /**
     * Touch threshold for corners and edges
     */
    private val touchThreshold = resources.getDimensionPixelSize(R.dimen.touch_threshold).toFloat()

    /**
     * Main rect which is drawn to canvas.
     */
    private val cropRect: AnimatableRectF =
        AnimatableRectF()

    /**
     * Temporary rect to animate crop rect to.
     * This value will be set to zero after using.
     */
    private val targetRect: AnimatableRectF =
        AnimatableRectF()

    /**
     * Minimum scale limitation is dependens on screen
     * and bitmap size. bitmapMinRect is calculated
     * initially. This value holds the miminum rectangle
     * which bitmapMatrix can be.
     */
    private val bitmapMinRect = RectF()

    /**
     * Minimum rectangle for cropRect can be.
     * This value will be only calculated on ACTION_DOWN.
     * Then will be check the crop rect value ACTION_MOVE and
     * override cropRect if it exceed its limit.
     */
    private val minRect = RectF()

    /**
     * Maximum rectangle for cropRect can be.
     * This value will be only calculated on ACTION_DOWN.
     * Then will be check the crop rect value ACTION_MOVE and
     * override cropRect if it exceed its limit.
     */
    private val maxRect = RectF()

    /**
     * Bitmap rect value. Holds original bitmap width
     * and height rectangle.
     */
    private val bitmapRect = RectF()

    /**
     * CropView rectangle. Holds view borders.
     */
    private val viewRect = RectF()

    /**
     * This value is hold view width minus margin between screen sides.
     * So it will be measuredWidth - dimen(R.dimen.default_crop_margin)
     */
    private var viewWidth = 0f

    /**
     * This value is hold view height minus margin between screen sides.
     * So it will be measuredWidth - dimen(R.dimen.default_crop_margin)
     */
    private var viewHeight = 0f

    /**
     * Original bitmap value
     */
    private var bitmap: Bitmap? = null

    /**
     * Bitmap matrix to draw bitmap on canvas
     */
    private val bitmapMatrix: Matrix = Matrix()

    /**
     * Empty paint to draw something on canvas.
     */
    private val emptyPaint = Paint().apply {
        isAntiAlias = true
    }

    /**
     * Default margin for cropRect.
     */
    private val marginInPixelSize =
        resources.getDimensionPixelSize(R.dimen.margin_max_crop_rect).toFloat()

    /**
     * User can drag crop rect from Corner, Edge or Bitmap
     */
    private var draggingState: DraggingState = DraggingState.Idle

    /**
     * Hold value for scaling bitmap with two finger.
     * We initialize this point to avoid memory
     * allocation every time user scale bitmap with fingers.
     */
    private val zoomFocusPoint = FloatArray(2)

    /**
     * This value holds inverted matrix when user scale
     * bitmap image with two finger. This value initialized to
     * avoid memory allocation every time user pinch zoom.
     */
    private val zoomInverseMatrix = Matrix()

    /**
     * Crop rect grid line width
     */
    private val gridLineWidthInPixel = resources.getDimension(R.dimen.grid_line_width)

    /**
     * Corner toggle line width
     */
    private val cornerToggleWidthInPixel = resources.getDimension(R.dimen.corner_toggle_width)

    /**
     * Corner toggle line length
     */
    private val cornerToggleLengthInPixel = resources.getDimension(R.dimen.corner_toggle_length)

    private val minRectLength = resources.getDimension(R.dimen.min_rect)

    /**
     * Mask color
     */
    private val maskBackgroundColor = ContextCompat.getColor(context, R.color.colorCropAlpha)

    /**
     * Mask canvas
     */
    private var maskCanvas: Canvas? = null

    /**
     * Mask bitmap
     */
    private var maskBitmap: Bitmap? = null

    private val bitmapGestureListener = object : BitmapGestureHandler.BitmapGestureListener {
        override fun onDoubleTap(motionEvent: MotionEvent) {

//            if (isBitmapScaleExceedMaxLimit(DOUBLE_TAP_SCALE_FACTOR)) {
//
//                val resetMatrix = Matrix()
//                val scale = max(
//                    cropRect.width() / bitmapRect.width(),
//                    cropRect.height() / bitmapRect.height()
//                )
//                resetMatrix.setScale(scale, scale)
//
//                val translateX = (viewWidth - bitmapRect.width() * scale) / 2f + marginInPixelSize
//                val translateY = (viewHeight - bitmapRect.height() * scale) / 2f + marginInPixelSize
//                resetMatrix.postTranslate(translateX, translateY)
//
//                bitmapMatrix.animateToMatrix(resetMatrix) {
//                    notifyCropRectChanged()
//                    invalidate()
//                }
//
//                return
//            }
//
//            bitmapMatrix.animateScaleToPoint(
//                DOUBLE_TAP_SCALE_FACTOR,
//                motionEvent.x,
//                motionEvent.y
//            ) {
//                notifyCropRectChanged()
//                invalidate()
//            }
        }

        override fun onScale(scaleFactor: Float, focusX: Float, focusY: Float) {
//
//            /**
//             * Return if new calculated bitmap matrix will exceed scale
//             * point then early return.
//             * Otherwise continue and do calculation and apply to bitmap matrix.
//             */
//            if (isBitmapScaleExceedMaxLimit(scaleFactor)) {
//                return
//            }
//
//            zoomInverseMatrix.reset()
//            bitmapMatrix.invert(zoomInverseMatrix)
//
//            /**
//             * Inverse focus points
//             */
//            zoomFocusPoint[0] = focusX
//            zoomFocusPoint[1] = focusY
//            zoomInverseMatrix.mapPoints(zoomFocusPoint)
//
//            /**
//             * Scale bitmap matrix
//             */
//            bitmapMatrix.preScale(
//                scaleFactor,
//                scaleFactor,
//                zoomFocusPoint[0],
//                zoomFocusPoint[1]
//            )
//            notifyCropRectChanged()
//
//            invalidate()
        }

        override fun onScroll(distanceX: Float, distanceY: Float) {
            cropRect.top -= distanceY
            cropRect.bottom -= distanceY

            notifyCropRectChanged()
            invalidate()
        }

        override fun onEnd() {
            settleDraggedBitmap()
        }
    }

    private val bitmapGestureHandler = BitmapGestureHandler(context, bitmapGestureListener)

    init {
        setWillNotDraw(false)
        setLayerType(LAYER_TYPE_HARDWARE, null)
        setBackgroundColor(ContextCompat.getColor(context, R.color.colorCropBackground))
    }

    /**
     * Initialize necessary rects, bitmaps, canvas here.
     */
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        initialize(null)
    }

    /**
     * Handles touches
     */
    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event == null)
            return false

        when (event.action) {
            ACTION_DOWN -> {
                setDraggingState(event)

                calculateMinRect()
                calculateMaxRect()
            }
            ACTION_MOVE -> {
                when (val state = draggingState) {
                    is DraggingCorner -> {
                        onCornerPositionChanged(state.corner, event)
                        updateExceedMaxBorders()
                        updateExceedMinBorders()
                        notifyCropRectChanged()
                    }
                    is DraggingEdge -> {
                        onEdgePositionChanged(state.edge, event)
                        updateExceedMaxBorders()
                        updateExceedMinBorders()
                        notifyCropRectChanged()
                    }
                    else -> Unit
                }
            }
            ACTION_UP -> {
                minRect.setEmpty()
                maxRect.setEmpty()
                when (draggingState) {
                    is DraggingEdge, is DraggingCorner -> {
                        calculateCenterTarget()

                        animateBitmapToCenterTarget()
                        animateCropRectToCenterTarget()
                    }
                    is DraggingState.DraggingCropRect -> {
                        calculateCenterTarget()

                        animateBitmapToCenterTarget()
//                        animateCropRectToCenterTarget()
                    }
                    else -> Unit
                }
            }
        }

        if (draggingState == DraggingState.DraggingCropRect)
            bitmapGestureHandler.onTouchEvent(event)

        invalidate()
        return true
    }

    private fun setDraggingState(event: MotionEvent){
        val corner by lazy { cropRect.getCornerTouch(event, touchThreshold) }
        val edge by lazy { cropRect.getEdgeTouch(event, touchThreshold) }

        draggingState = when {
            corner != Corner.NONE -> DraggingCorner(corner)
            edge != Edge.NONE -> DraggingEdge(edge)
            event.withinRectangle(cropRect) -> DraggingState.DraggingCropRect
            else -> DraggingState.Idle
        }
    }

    /**
     * Draw bitmap, cropRect, overlay
     */
    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        bitmap?.let { bitmap ->
            canvas?.drawBitmap(bitmap, bitmapMatrix, emptyPaint)
        }

        canvas?.save()
        canvas?.clipRect(cropRect)
        canvas?.drawColor(maskBackgroundColor)
        canvas?.restore()

        drawGrid(canvas)
//        drawCornerToggles(canvas)
    }

    /**
     * Set bitmap from outside of this view.
     * Calculates bitmap rect and bitmap min rect.
     */
    fun setBitmap(bitmap: Bitmap?, initialCropRect: Rect?) {
        this.bitmap = bitmap

        bitmapRect.set(
            0f,
            0f,
            this.bitmap?.width?.toFloat() ?: 0f,
            this.bitmap?.height?.toFloat() ?: 0f
        )

        val bitmapMinRectSize = max(bitmapRect.width(), bitmapRect.height()) / MAX_SCALE
        bitmapMinRect.set(0f, 0f, bitmapMinRectSize, bitmapMinRectSize)

        initialize(initialCropRect)

        requestLayout()
        invalidate()
    }

    fun setTheme(croppyTheme: CroppyTheme) {
        highlightPaint.color = ContextCompat.getColor(context, croppyTheme.accentColor)
    }

    fun getCropRect(): Rect {
        val croppedBitmapRect = getCropSizeOriginal()

        if (bitmapRect.intersect(croppedBitmapRect).not()) {
            return croppedBitmapRect.toRect()
        }

        val cropLeft = if (croppedBitmapRect.left.roundToInt() < bitmapRect.left) {
            bitmapRect.left.toInt()
        } else {
            croppedBitmapRect.left.roundToInt()
        }

        val cropTop = if (croppedBitmapRect.top.roundToInt() < bitmapRect.top) {
            bitmapRect.top.toInt()
        } else {
            croppedBitmapRect.top.roundToInt()
        }

        val cropRight = if (croppedBitmapRect.right.roundToInt() > bitmapRect.right) {
            bitmapRect.right.toInt()
        } else {
            croppedBitmapRect.right.roundToInt()
        }

        val cropBottom = if (croppedBitmapRect.bottom.roundToInt() > bitmapRect.bottom) {
            bitmapRect.bottom.toInt()
        } else {
            croppedBitmapRect.bottom.roundToInt()
        }

        if (bitmap != null)
            return Rect(cropLeft, cropTop, cropRight, cropBottom)
        throw IllegalStateException("Bitmap is null.")
    }

    /**
     * Current crop size depending on original bitmap.
     * Returns rectangle as pixel values.
     */
    fun getCropSizeOriginal(): RectF {
        val cropSizeOriginal = RectF()
        cropRectOnOriginalBitmapMatrix.reset()
        bitmapMatrix.invert(cropRectOnOriginalBitmapMatrix)
        cropRectOnOriginalBitmapMatrix.mapRect(cropSizeOriginal, cropRect)
        return cropSizeOriginal
    }

    /**
     * Initialize
     */
    private fun initialize(initialCropRect: Rect?) {
        viewWidth = measuredWidth.toFloat() - (marginInPixelSize * 2)
        viewHeight = measuredHeight.toFloat() - (marginInPixelSize * 2)

        viewRect.set(0f, 0f, measuredWidth.toFloat(), measuredHeight.toFloat())

        createMaskBitmap()
        initializeBitmapMatrix()
        initializeCropRect(initialCropRect)

        onInitialized?.invoke()

        invalidate()
    }

    /**
     * Create mask bitmap
     */
    private fun createMaskBitmap() {
        maskBitmap = Bitmap.createBitmap(measuredWidth, measuredHeight, Bitmap.Config.ARGB_8888)
        maskCanvas = Canvas(maskBitmap!!)
    }

    private val cropPaint = Paint().apply {
        color = Color.WHITE
        strokeWidth = gridLineWidthInPixel
        style = Paint.Style.STROKE
    }

    private val highlightPaint = Paint().apply {
        color = Color.WHITE
        strokeWidth = 3F
        style = Paint.Style.STROKE
    }

    /**
     * Draw crop rect as a grid.
     */
    private fun drawGrid(canvas: Canvas?) {

        /**
         * Primary, outer rectangle
         */

        canvas?.drawLine(
            cropRect.left,
            cropRect.top,
            cropRect.right,
            cropRect.top,
            highlightPaint
        )

        canvas?.drawLine(
            cropRect.left,
            cropRect.bottom,
            cropRect.right,
            cropRect.bottom,
            highlightPaint
        )

        canvas?.drawLine(
            cropRect.left,
            cropRect.bottom,
            cropRect.left,
            cropRect.top,
            cropPaint
        )

        canvas?.drawLine(
            cropRect.right,
            cropRect.bottom,
            cropRect.right,
            cropRect.top,
            cropPaint
        )

        /**
         * Inner lines
         */

        canvas?.drawLine(
            cropRect.left + cropRect.width() / 3f,
            cropRect.top,
            cropRect.left + cropRect.width() / 3f,
            cropRect.bottom,
            cropPaint
        )

        canvas?.drawLine(
            cropRect.left + cropRect.width() * 2f / 3f,
            cropRect.top,
            cropRect.left + cropRect.width() * 2f / 3f,
            cropRect.bottom,
            cropPaint
        )

        canvas?.drawLine(
            cropRect.left,
            cropRect.top + cropRect.height() / 3f,
            cropRect.right,
            cropRect.top + cropRect.height() / 3f,
            cropPaint
        )

        canvas?.drawLine(
            cropRect.left,
            cropRect.top + cropRect.height() * 2f / 3f,
            cropRect.right,
            cropRect.top + cropRect.height() * 2f / 3f,
            cropPaint
        )
    }

    /**
     * Draw corner lines and toggles
     */
//    private fun drawCornerToggles(canvas: Canvas?) {
//        /**
//         * Top left toggle
//         */
//        canvas?.drawLine(
//            cropRect.left - gridLineWidthInPixel,
//            cropRect.top + cornerToggleWidthInPixel / 2f - gridLineWidthInPixel,
//            cropRect.left + cornerToggleLengthInPixel,
//            cropRect.top + cornerToggleWidthInPixel / 2f - gridLineWidthInPixel,
//            highlightPaint
//        )
//
//        canvas?.drawLine(
//            cropRect.left + cornerToggleWidthInPixel / 2f - gridLineWidthInPixel,
//            cropRect.top - gridLineWidthInPixel,
//            cropRect.left + cornerToggleWidthInPixel / 2f - gridLineWidthInPixel,
//            cropRect.top + cornerToggleLengthInPixel,
//            highlightPaint
//        )
//
//        /**
//         * Top Right toggle
//         */
//
//        canvas?.drawLine(
//            cropRect.right - cornerToggleLengthInPixel,
//            cropRect.top + cornerToggleWidthInPixel / 2f - gridLineWidthInPixel,
//            cropRect.right + gridLineWidthInPixel,
//            cropRect.top + cornerToggleWidthInPixel / 2f - gridLineWidthInPixel,
//            highlightPaint
//        )
//
//        canvas?.drawLine(
//            cropRect.right - cornerToggleWidthInPixel / 2f + gridLineWidthInPixel,
//            cropRect.top - gridLineWidthInPixel,
//            cropRect.right - cornerToggleWidthInPixel / 2f + gridLineWidthInPixel,
//            cropRect.top + cornerToggleLengthInPixel,
//            highlightPaint
//        )
//
//        /**
//         * Bottom Left toggle
//         */
//
//        canvas?.drawLine(
//            cropRect.left - gridLineWidthInPixel,
//            cropRect.bottom - cornerToggleWidthInPixel / 2f + gridLineWidthInPixel,
//            cropRect.left + cornerToggleLengthInPixel,
//            cropRect.bottom - cornerToggleWidthInPixel / 2f + gridLineWidthInPixel,
//            highlightPaint
//        )
//
//        canvas?.drawLine(
//            cropRect.left + cornerToggleWidthInPixel / 2f - gridLineWidthInPixel,
//            cropRect.bottom + gridLineWidthInPixel,
//            cropRect.left + cornerToggleWidthInPixel / 2f - gridLineWidthInPixel,
//            cropRect.bottom - cornerToggleLengthInPixel,
//            highlightPaint
//        )
//
//        /**
//         * Bottom Right toggle
//         */
//        canvas?.drawLine(
//            cropRect.right - cornerToggleLengthInPixel,
//            cropRect.bottom - cornerToggleWidthInPixel / 2f + gridLineWidthInPixel,
//            cropRect.right + gridLineWidthInPixel,
//            cropRect.bottom - cornerToggleWidthInPixel / 2f + gridLineWidthInPixel,
//            highlightPaint
//        )
//
//        canvas?.drawLine(
//            cropRect.right - cornerToggleWidthInPixel / 2f + gridLineWidthInPixel,
//            cropRect.bottom + gridLineWidthInPixel,
//            cropRect.right - cornerToggleWidthInPixel / 2f + gridLineWidthInPixel,
//            cropRect.bottom - cornerToggleLengthInPixel,
//            highlightPaint
//        )
//    }

    /**
     * Initializes bitmap matrix
     */
    private fun initializeBitmapMatrix() {
        val scale = min(viewWidth / bitmapRect.width(), viewHeight / bitmapRect.height())
        bitmapMatrix.setScale(scale, scale)

        val translateX = (viewWidth - bitmapRect.width() * scale) / 2f + marginInPixelSize
        val translateY = (viewHeight - bitmapRect.height() * scale) / 2f + marginInPixelSize
        bitmapMatrix.postTranslate(translateX, translateY)
    }

    /**
     * Initializes crop rect with bitmap.
     */
    private fun initializeCropRect(initialCropRect: Rect?) {
        bitmapMatrix.mapRect(
            cropRect,
            initialCropRect?.toRectF() ?: RectF(0f, 0f, bitmapRect.width(), bitmapRect.height())
        )
    }

    /**
     * Move cropRect on user drag cropRect from corners.
     * Corner will be move to opposite side of the selected cropRect's
     * corner. If aspect ratio selected (Not free), then aspect ration shouldn't
     * be change on cropRect is changed.
     */
    private fun onCornerPositionChanged(corner: Corner, motionEvent: MotionEvent) {
        when (corner) {
            TOP_RIGHT, TOP_LEFT -> cropRect.top = motionEvent.y
            BOTTOM_RIGHT, BOTTOM_LEFT -> cropRect.bottom = motionEvent.y
            else -> return
        }
    }

    /**
     * Move cropRect on user drag cropRect from edges.
     * Corner will be move to opposite side of the selected cropRect's
     * edge. If aspect ratio selected (Not free), then aspect ration shouldn't
     * be change on cropRect is changed.
     */
    private fun onEdgePositionChanged(edge: Edge, motionEvent: MotionEvent) {
        val bitmapBorderRect = RectF()
        bitmapMatrix.mapRect(bitmapBorderRect, bitmapRect)

        when (edge) {
            TOP -> cropRect.top = motionEvent.y
            BOTTOM -> cropRect.bottom = motionEvent.y
            else -> return
        }
    }

    /**
     * Calculates minimum possible rectangle that user can drag
     * cropRect
     */
    private fun calculateMinRect() {
        val mappedBitmapMinRectSize = RectF()
            .apply { bitmapMatrix.mapRect(this, bitmapMinRect) }
            .width()

        val minSize = max(mappedBitmapMinRectSize, minRectLength)

        when (val state = draggingState) {
            is DraggingEdge -> {
                when (state.edge) {
                    LEFT -> minRect.set(
                        cropRect.right - minSize,
                        cropRect.top,
                        cropRect.right,
                        cropRect.bottom
                    )
                    TOP -> minRect.set(
                        cropRect.left,
                        cropRect.bottom - minSize,
                        cropRect.right,
                        cropRect.bottom
                    )
                    RIGHT -> minRect.set(
                        cropRect.left,
                        cropRect.top,
                        cropRect.left + minSize,
                        cropRect.bottom
                    )
                    BOTTOM -> minRect.set(
                        cropRect.left,
                        cropRect.top,
                        cropRect.right,
                        cropRect.top + minSize
                    )
                }
            }
            is DraggingCorner -> {
                when (state.corner) {
                    TOP_RIGHT -> minRect.set(
                        cropRect.left,
                        cropRect.bottom - minSize,
                        cropRect.left + minSize,
                        cropRect.bottom
                    )
                    TOP_LEFT -> minRect.set(
                        cropRect.right - minSize,
                        cropRect.bottom - minSize,
                        cropRect.right,
                        cropRect.bottom
                    )
                    BOTTOM_RIGHT -> minRect.set(
                        cropRect.left,
                        cropRect.top,
                        cropRect.left + minSize,
                        cropRect.top + minSize
                    )
                    BOTTOM_LEFT -> minRect.set(
                        cropRect.right - minSize,
                        cropRect.top,
                        cropRect.right,
                        cropRect.top + minSize
                    )
                }
            }
        }
    }

    /**
     * Calculates maximum possible rectangle that user can
     * drag cropRect
     */
    private fun calculateMaxRect() {
        val borderRect = RectF().apply {
            val bitmapBorderRect = RectF()
            bitmapMatrix.mapRect(bitmapBorderRect, bitmapRect)
            top = max(bitmapBorderRect.top, viewRect.top)
            right = min(bitmapBorderRect.right, viewRect.right)
            bottom = min(bitmapBorderRect.bottom, viewRect.bottom)
            left = max(bitmapBorderRect.left, viewRect.left)
        }

        when (val state = draggingState) {
            is DraggingEdge -> {
                when (state.edge) {
                    LEFT -> maxRect.set(
                        borderRect.left,
                        cropRect.top,
                        cropRect.right,
                        cropRect.bottom
                    )
                    TOP -> maxRect.set(
                        cropRect.left,
                        borderRect.top,
                        cropRect.right,
                        cropRect.bottom
                    )
                    RIGHT -> maxRect.set(
                        cropRect.left,
                        cropRect.top,
                        borderRect.right,
                        cropRect.bottom
                    )
                    BOTTOM -> maxRect.set(
                        cropRect.left,
                        cropRect.top,
                        cropRect.right,
                        borderRect.bottom
                    )
                }
            }
            is DraggingCorner -> {
                when (state.corner) {
                    TOP_RIGHT -> maxRect.set(
                        cropRect.left,
                        borderRect.top,
                        borderRect.right,
                        cropRect.bottom
                    )
                    TOP_LEFT -> maxRect.set(
                        borderRect.left,
                        borderRect.top,
                        cropRect.right,
                        cropRect.bottom
                    )
                    BOTTOM_RIGHT -> maxRect.set(
                        cropRect.left,
                        cropRect.top,
                        borderRect.right,
                        borderRect.bottom
                    )
                    BOTTOM_LEFT -> maxRect.set(
                        borderRect.left,
                        cropRect.top,
                        cropRect.right,
                        borderRect.bottom
                    )
                }
            }
        }
    }

    /**
     * If user exceed its limit we override cropRect borders
     */
    private fun updateExceedMaxBorders() {
        if (cropRect.left < maxRect.left) {
            cropRect.left = maxRect.left
        }

        if (cropRect.top < maxRect.top) {
            cropRect.top = maxRect.top
        }

        if (cropRect.right > maxRect.right) {
            cropRect.right = maxRect.right
        }

        if (cropRect.bottom > maxRect.bottom) {
            cropRect.bottom = maxRect.bottom
        }
    }

    /**
     * If user exceed its limit we override cropRect borders
     */
    private fun updateExceedMinBorders() {
        if (cropRect.left > minRect.left) {
            cropRect.left = minRect.left
        }

        if (cropRect.top > minRect.top) {
            cropRect.top = minRect.top
        }

        if (cropRect.right < minRect.right) {
            cropRect.right = minRect.right
        }

        if (cropRect.bottom < minRect.bottom) {
            cropRect.bottom = minRect.bottom
        }
    }

    /**
     * If user minimizes the crop rect, we need to
     * calculate target centered rectangle according to
     * current cropRect aspect ratio and size. With this
     * target rectangle, we can animate crop rect to
     * center target. and also we can animate bitmap matrix
     * to selected cropRect using this target rectangle.
     */
    private fun calculateCenterTarget() {
        val heightScale = viewHeight / cropRect.height()
        val widthScale = viewWidth / cropRect.width()
        val scale = min(heightScale, widthScale)

        val targetRectWidth = cropRect.width() * scale
        val targetRectHeight = cropRect.height() * scale

        val targetRectLeft = (viewWidth - targetRectWidth) / 2f + marginInPixelSize
        val targetRectTop = (viewHeight - targetRectHeight) / 2f + marginInPixelSize
        val targetRectRight = targetRectLeft + targetRectWidth
        val targetRectBottom = targetRectTop + targetRectHeight

        targetRect.set(targetRectLeft, targetRectTop, targetRectRight, targetRectBottom)
    }

    /**
     * When user changes cropRect size by dragging it, cropRect
     * should be animated to center without changing aspect ratio,
     * meanwhile bitmap matrix should be take selected crop rect to
     * the center. This methods take selected crop rect to the cennter.
     */
    private fun animateBitmapToCenterTarget() {
        val newBitmapMatrix = bitmapMatrix.clone()

        val scale = targetRect.width() / cropRect.width()
        val translateX = targetRect.centerX() - cropRect.centerX()
        val translateY = targetRect.centerY() - cropRect.centerY()

        val matrix = Matrix()
        matrix.setScale(scale, scale, cropRect.centerX(), cropRect.centerY())
        matrix.postTranslate(translateX, translateY)
        newBitmapMatrix.postConcat(matrix)

        bitmapMatrix.animateToMatrix(newBitmapMatrix) {
            invalidate()
        }
    }

    /**
     * Animates current crop rect to the center position
     */
    private fun animateCropRectToCenterTarget() {
        cropRect.animateTo(targetRect) {
            invalidate()
            notifyCropRectChanged()
        }
    }

    /**
     * when user drag bitmap too much, we need to settle bitmap matrix
     * back to the possible closest edge.
     */
    private fun settleDraggedBitmap() {
        val draggedBitmapRect = RectF()
        bitmapMatrix.mapRect(draggedBitmapRect, bitmapRect)

        /**
         * Scale dragged matrix if it needs to
         */
        val widthScale = cropRect.width() / draggedBitmapRect.width()
        val heightScale = cropRect.height() / draggedBitmapRect.height()
        var scale = 1.0f

        if (widthScale > 1.0f || heightScale > 1.0f) {
            scale = max(widthScale, heightScale)
        }

        /**
         * Calculate new scaled matrix for dragged bitmap matrix
         */
        val scaledRect = RectF()
        val scaledMatrix = Matrix()
        scaledMatrix.setScale(scale, scale)
        scaledMatrix.mapRect(scaledRect, draggedBitmapRect)


        /**
         * Calculate translateX
         */
        var translateX = 0f
        if (scaledRect.left > cropRect.left) {
            translateX = cropRect.left - scaledRect.left
        }

        if (scaledRect.right < cropRect.right) {
            translateX = cropRect.right - scaledRect.right
        }

        /**
         * Calculate translateX
         */
        var translateY = 0f
        if (scaledRect.top > cropRect.top) {
            translateY = cropRect.top - scaledRect.top
        }

        if (scaledRect.bottom < cropRect.bottom) {
            translateY = cropRect.bottom - scaledRect.bottom
        }

        /**
         * New temp bitmap matrix
         */
        val newBitmapMatrix = bitmapMatrix.clone()

        val matrix = Matrix()
        matrix.setScale(scale, scale)
        matrix.postTranslate(translateX, translateY)
        newBitmapMatrix.postConcat(matrix)

        bitmapMatrix.animateToMatrix(newBitmapMatrix) {
            invalidate()
            notifyCropRectChanged()
        }
    }

    /**
     * Pretend a bitmap matrix value if scale factor will be applied to
     * bitmap matrix. , then returns
     * true, false otherwise.
     * @return true If pretended value is exceed max scale value, false otherwise
     */
    private fun isBitmapScaleExceedMaxLimit(scaleFactor: Float): Boolean {
        val bitmapMatrixCopy = bitmapMatrix.clone()
        bitmapMatrixCopy.preScale(scaleFactor, scaleFactor)

        val invertedBitmapMatrix = Matrix()
        bitmapMatrixCopy.invert(invertedBitmapMatrix)

        val invertedBitmapCropRect = RectF()

        invertedBitmapMatrix.mapRect(invertedBitmapCropRect, cropRect)
        return min(
            invertedBitmapCropRect.width(),
            invertedBitmapCropRect.height()
        ) <= bitmapMinRect.width()
    }

    private fun notifyCropRectChanged() {
        observeCropRectOnOriginalBitmapChanged?.invoke(getCropSizeOriginal())
    }

    companion object {

        /**
         * Maximum scale for given bitmap
         */
        private const val MAX_SCALE = 15f

        /**
         * Use this constant, when user double tap to scale
         */
        private const val DOUBLE_TAP_SCALE_FACTOR = 2f

    }
}