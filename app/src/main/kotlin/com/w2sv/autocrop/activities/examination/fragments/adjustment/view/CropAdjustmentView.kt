package com.w2sv.autocrop.activities.examination.fragments.adjustment.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.MotionEvent.ACTION_DOWN
import android.view.MotionEvent.ACTION_MOVE
import android.view.MotionEvent.ACTION_UP
import android.view.View
import androidx.core.content.ContextCompat
import com.w2sv.androidutils.extensions.postValue
import com.w2sv.androidutils.extensions.viewModel
import com.w2sv.autocrop.R
import com.w2sv.autocrop.activities.examination.fragments.adjustment.CropAdjustmentFragment
import com.w2sv.autocrop.activities.examination.fragments.adjustment.extensions.animateToMatrix
import com.w2sv.autocrop.activities.examination.fragments.adjustment.extensions.clone
import com.w2sv.autocrop.activities.examination.fragments.adjustment.extensions.getCornerTouch
import com.w2sv.autocrop.activities.examination.fragments.adjustment.extensions.getEdgeTouch
import com.w2sv.autocrop.activities.examination.fragments.adjustment.extensions.maxRectFFrom
import com.w2sv.autocrop.activities.examination.fragments.adjustment.extensions.minRectFFrom
import com.w2sv.autocrop.activities.examination.fragments.adjustment.extensions.withinRectangle
import com.w2sv.autocrop.activities.examination.fragments.adjustment.model.AnimatableRectF
import com.w2sv.autocrop.activities.examination.fragments.adjustment.model.Corner
import com.w2sv.autocrop.activities.examination.fragments.adjustment.model.Corner.BOTTOM_LEFT
import com.w2sv.autocrop.activities.examination.fragments.adjustment.model.Corner.BOTTOM_RIGHT
import com.w2sv.autocrop.activities.examination.fragments.adjustment.model.Corner.TOP_LEFT
import com.w2sv.autocrop.activities.examination.fragments.adjustment.model.Corner.TOP_RIGHT
import com.w2sv.autocrop.activities.examination.fragments.adjustment.model.DraggingState
import com.w2sv.autocrop.activities.examination.fragments.adjustment.model.DraggingState.DraggingCorner
import com.w2sv.autocrop.activities.examination.fragments.adjustment.model.DraggingState.DraggingEdge
import com.w2sv.autocrop.activities.examination.fragments.adjustment.model.Edge
import com.w2sv.autocrop.activities.examination.fragments.adjustment.model.Edge.BOTTOM
import com.w2sv.autocrop.activities.examination.fragments.adjustment.model.Edge.LEFT
import com.w2sv.autocrop.activities.examination.fragments.adjustment.model.Edge.RIGHT
import com.w2sv.autocrop.activities.examination.fragments.adjustment.model.Edge.TOP
import com.w2sv.autocrop.cropbundle.cropping.CropEdges
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

class CropAdjustmentView(context: Context, attrs: AttributeSet) : View(context, attrs) {

    /**
     * Main rect which is drawn to canvas.
     */
    private var cropRect: AnimatableRectF =
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
     * Bitmap matrix to draw bitmap on canvas
     */
    private val bitmapMatrix: Matrix = Matrix()

    /**
     * User can drag crop rect from Corner, Edge or Bitmap
     */
    private var draggingState: DraggingState = DraggingState.Idle

    private val bitmapBorderRect = RectF()

    companion object {
        private const val BITMAP_MAX_SCALE = 15f

        private const val TOUCH_THRESHOLD: Float = 16f

        private const val CROP_RECT_MARGIN: Float = 24f

        private const val GRID_LINE_WIDTH: Float = 1f

        private const val CORNER_WIDTH: Float = 3f

        private const val CORNER_EDGE_LENGTH: Int = 16

        private const val MIN_RECT_SIZE: Float = 56f
    }

    private val viewModel by viewModel<CropAdjustmentFragment.ViewModel>()

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        if (!isInEditMode) {
            setWillNotDraw(false)

            bitmapRect.set(
                0f,
                0f,
                viewModel.bitmap.width.toFloat(),
                viewModel.bitmap.height.toFloat(),
            )
        }
    }

    private fun initializeView() {
        val bitmapMinRectSize = max(bitmapRect.width(), bitmapRect.height()) / BITMAP_MAX_SCALE
        bitmapMinRect.set(0f, 0f, bitmapMinRectSize, bitmapMinRectSize)

        viewWidth = measuredWidth.toFloat() - (CROP_RECT_MARGIN * 2)
        viewHeight = measuredHeight.toFloat() - (CROP_RECT_MARGIN * 2)

        viewRect.set(0f, 0f, measuredWidth.toFloat(), measuredHeight.toFloat())

        initializeBitmapMatrix()
        initializeCropRect()

        requestLayout()
        invalidate()
    }

    fun reset() {
        initializeView()
        onCropRectChanged()
    }

    private val bitmapGestureHandler = BitmapGestureHandler(
        context,
        object : BitmapGestureHandler.BitmapGestureListener {
            override fun onScroll(distanceX: Float, distanceY: Float) {
                val topNew = cropRect.top - distanceY
                val bottomNew = cropRect.bottom - distanceY

                if (topNew > bitmapBorderRect.top && bottomNew < bitmapBorderRect.bottom) {
                    cropRect.top = topNew
                    cropRect.bottom = bottomNew

                    onCropRectChanged()
                    invalidate()
                }
            }
        }
    )

    /**
     * Initialize necessary rects, bitmaps, canvas here.
     */
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        initializeView()
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

                if (draggingState !is DraggingState.Idle) {
                    setBitmapBorderRect()

                    calculateMinRect()
                    calculateMaxRect()
                }
            }

            ACTION_MOVE -> {
                when (val state = draggingState) {
                    is DraggingEdge -> {
                        onEdgePositionChanged(state.edge, event)
                        updateExceedMaxBorders()
                        updateExceedMinBorders()
                        onCropRectChanged()
                    }

                    else -> Unit
                }
            }

            ACTION_UP -> {
                minRect.setEmpty()
                maxRect.setEmpty()
                when (draggingState) {
                    is DraggingEdge, is DraggingState.DraggingCropRect -> {
                        calculateCenterTarget()

                        animateBitmapToCenterTarget()
                        animateCropRectToCenterTarget()
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

    private fun setDraggingState(event: MotionEvent) {
        val corner by lazy { cropRect.getCornerTouch(event, TOUCH_THRESHOLD) }
        val edge by lazy { cropRect.getEdgeTouch(event, TOUCH_THRESHOLD) }

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

        canvas?.apply {
            drawBitmap(viewModel.bitmap, bitmapMatrix, emptyPaint)
            save()
            clipRect(cropRect)
            drawColor(ContextCompat.getColor(context, R.color.crop_mask))
            restore()

            //            drawCropEdgePairCandidates()
            drawGrid()
            drawCorners()
        }
    }

    private val emptyPaint = Paint().apply {
        isAntiAlias = true
    }

    //    private fun Canvas.drawCropEdgePairCandidates(){
    //        rescaledCropEdgeRectFs.forEach {
    //            drawLine(
    //                it.left,
    //                it.top,
    //                it.right,
    //                it.top,
    //                topEdgePaint
    //            )
    //
    //            drawLine(
    //                it.left,
    //                it.bottom,
    //                it.right,
    //                it.bottom,
    //                bottomEdgePaint
    //            )
    //        }
    //    }

    //    private val topEdgePaint = Paint().apply {
    //        color = Color.GREEN
    //        strokeWidth = gridLineWidthPixel
    //        style = Paint.Style.STROKE
    //    }
    //
    //    private val bottomEdgePaint = Paint().apply {
    //        color = Color.MAGENTA
    //        strokeWidth = gridLineWidthPixel
    //        style = Paint.Style.STROKE
    //    }

    /**
     * Draw crop rect as a grid.
     */
    private fun Canvas.drawGrid() {

        /**
         * Primary, outer rectangle
         */

        drawLine(
            cropRect.left,
            cropRect.bottom,
            cropRect.left,
            cropRect.top,
            gridPaint
        )

        drawLine(
            cropRect.right,
            cropRect.bottom,
            cropRect.right,
            cropRect.top,
            gridPaint
        )

        drawLine(
            cropRect.left,
            cropRect.top,
            cropRect.right,
            cropRect.top,
            accentPaint
        )

        drawLine(
            cropRect.left,
            cropRect.bottom,
            cropRect.right,
            cropRect.bottom,
            accentPaint
        )

        /**
         * Inner lines
         */

        drawLine(
            cropRect.left + cropRect.width() / 3f,
            cropRect.top,
            cropRect.left + cropRect.width() / 3f,
            cropRect.bottom,
            gridPaint
        )

        drawLine(
            cropRect.left + cropRect.width() * 2f / 3f,
            cropRect.top,
            cropRect.left + cropRect.width() * 2f / 3f,
            cropRect.bottom,
            gridPaint
        )

        drawLine(
            cropRect.left,
            cropRect.top + cropRect.height() / 3f,
            cropRect.right,
            cropRect.top + cropRect.height() / 3f,
            gridPaint
        )

        drawLine(
            cropRect.left,
            cropRect.top + cropRect.height() * 2f / 3f,
            cropRect.right,
            cropRect.top + cropRect.height() * 2f / 3f,
            gridPaint
        )
    }

    private fun Canvas.drawCorners() {
        /**
         * Top left
         */
        drawLine(
            cropRect.left - GRID_LINE_WIDTH,
            cropRect.top + CORNER_WIDTH / 2f - GRID_LINE_WIDTH,
            cropRect.left + CORNER_EDGE_LENGTH,
            cropRect.top + CORNER_WIDTH / 2f - GRID_LINE_WIDTH,
            cornerPaint
        )

        drawLine(
            cropRect.left + CORNER_WIDTH / 2f - GRID_LINE_WIDTH,
            cropRect.top - GRID_LINE_WIDTH,
            cropRect.left + CORNER_WIDTH / 2f - GRID_LINE_WIDTH,
            cropRect.top + CORNER_EDGE_LENGTH,
            cornerPaint
        )

        /**
         * Top Right
         */

        drawLine(
            cropRect.right - CORNER_EDGE_LENGTH,
            cropRect.top + CORNER_WIDTH / 2f - GRID_LINE_WIDTH,
            cropRect.right + GRID_LINE_WIDTH,
            cropRect.top + CORNER_WIDTH / 2f - GRID_LINE_WIDTH,
            cornerPaint
        )

        drawLine(
            cropRect.right - CORNER_WIDTH / 2f + GRID_LINE_WIDTH,
            cropRect.top - GRID_LINE_WIDTH,
            cropRect.right - CORNER_WIDTH / 2f + GRID_LINE_WIDTH,
            cropRect.top + CORNER_EDGE_LENGTH,
            cornerPaint
        )

        /**
         * Bottom Left
         */

        drawLine(
            cropRect.left - GRID_LINE_WIDTH,
            cropRect.bottom - CORNER_WIDTH / 2f + GRID_LINE_WIDTH,
            cropRect.left + CORNER_EDGE_LENGTH,
            cropRect.bottom - CORNER_WIDTH / 2f + GRID_LINE_WIDTH,
            cornerPaint
        )

        drawLine(
            cropRect.left + CORNER_WIDTH / 2f - GRID_LINE_WIDTH,
            cropRect.bottom + GRID_LINE_WIDTH,
            cropRect.left + CORNER_WIDTH / 2f - GRID_LINE_WIDTH,
            cropRect.bottom - CORNER_EDGE_LENGTH,
            cornerPaint
        )

        /**
         * Bottom Right
         */
        drawLine(
            cropRect.right - CORNER_EDGE_LENGTH,
            cropRect.bottom - CORNER_WIDTH / 2f + GRID_LINE_WIDTH,
            cropRect.right + GRID_LINE_WIDTH,
            cropRect.bottom - CORNER_WIDTH / 2f + GRID_LINE_WIDTH,
            cornerPaint
        )

        drawLine(
            cropRect.right - CORNER_WIDTH / 2f + GRID_LINE_WIDTH,
            cropRect.bottom + GRID_LINE_WIDTH,
            cropRect.right - CORNER_WIDTH / 2f + GRID_LINE_WIDTH,
            cropRect.bottom - CORNER_EDGE_LENGTH,
            cornerPaint
        )
    }

    private val gridPaint = Paint().apply {
        color = Color.WHITE
        strokeWidth = GRID_LINE_WIDTH
        style = Paint.Style.STROKE
    }

    private val accentPaint: Paint by lazy {
        Paint().apply {
            color = context.getColor(R.color.magenta_saturated)
            strokeWidth = 3F
            style = Paint.Style.FILL
        }
    }

    private val cornerPaint: Paint by lazy {
        Paint().apply {
            color = context.getColor(R.color.magenta_saturated)
            strokeWidth = 6F
            style = Paint.Style.FILL
        }
    }

    private fun initializeBitmapMatrix() {
        val scale = min(viewWidth / bitmapRect.width(), viewHeight / bitmapRect.height())
        bitmapMatrix.setScale(scale, scale)

        val translateX = (viewWidth - bitmapRect.width() * scale) / 2f + CROP_RECT_MARGIN
        val translateY = (viewHeight - bitmapRect.height() * scale) / 2f + CROP_RECT_MARGIN
        bitmapMatrix.postTranslate(translateX, translateY)

        setBitmapBorderRect()
        //        setRescaledCropEdgePairCandidates()
    }

    private fun setBitmapBorderRect() {
        bitmapMatrix.mapRect(bitmapBorderRect, bitmapRect)
    }

    //    private lateinit var rescaledCropEdgeRectFs: List<RectF>

    //    private fun setRescaledCropEdgePairCandidates(){
    //        rescaledCropEdgeRectFs = viewModelImmediate.cropEdgePairCandidates.map {
    //            it.asRectF(viewModelImmediate.bitmap.width).apply {
    //                bitmapMatrix.mapRect(this)
    //            }
    //        }
    //    }

    /**
     * Initializes crop rect with bitmap.
     */
    private fun initializeCropRect() {
        bitmapMatrix.mapRect(
            cropRect,
            viewModel.initialCropEdges.asRectF(viewModel.bitmap.width)
        )
    }

    /**
     * Move cropRect on user drag cropRect from edges.
     * Corner will be move to opposite side of the selected cropRect's
     * edge. If aspect ratio selected (Not free), then aspect ration shouldn't
     * be change on cropRect is changed.
     */
    private fun onEdgePositionChanged(edge: Edge, motionEvent: MotionEvent) {
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
        val minSize = max(
            RectF()
                .apply { bitmapMatrix.mapRect(this, bitmapMinRect) }
                .width(),
            MIN_RECT_SIZE
        )

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

                    else -> Unit
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

                    else -> Unit
                }
            }

            else -> Unit
        }
    }

    /**
     * Calculates maximum possible rectangle that user can
     * drag cropRect
     */
    private fun calculateMaxRect() {
        val borderRect = maxRectFFrom(bitmapBorderRect, viewRect)

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

                    else -> {}
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

                    else -> {}
                }
            }

            else -> {}
        }
    }

    private fun updateExceedMaxBorders() {
        cropRect.set(maxRectFFrom(cropRect, maxRect))
    }

    private fun updateExceedMinBorders() {
        cropRect.set(minRectFFrom(cropRect, minRect))
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

        val targetRectLeft = (viewWidth - targetRectWidth) / 2f + CROP_RECT_MARGIN
        val targetRectTop = (viewHeight - targetRectHeight) / 2f + CROP_RECT_MARGIN
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
            //            setRescaledCropEdgePairCandidates()
            invalidate()
        }
    }

    /**
     * Animates current crop rect to the center position
     */
    private fun animateCropRectToCenterTarget() {
        cropRect.animateTo(targetRect) {
            invalidate()
        }
    }

    private fun onCropRectChanged() {
        viewModel.cropEdges.postValue(
            getCropRect().run {
                CropEdges(top.roundToInt(), bottom.roundToInt())
            }
        )
    }

    /**
     * Current crop size depending on original bitmap.
     * Returns rectangle as pixel values.
     */
    private fun getCropRect(): RectF =
        RectF().apply {
            val cropRectOnOriginalBitmapMatrix = Matrix()
                .apply {
                    bitmapMatrix.invert(this)
                }
            cropRectOnOriginalBitmapMatrix.mapRect(this, cropRect)
        }
}