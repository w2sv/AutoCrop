package com.w2sv.autocrop.activities.examination.fragments.adjustment

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.MotionEvent.ACTION_DOWN
import android.view.MotionEvent.ACTION_MOVE
import android.view.MotionEvent.ACTION_UP
import android.view.View
import androidx.core.content.ContextCompat
import com.w2sv.androidutils.extensions.postValue
import com.w2sv.androidutils.extensions.viewModel
import com.w2sv.autocrop.R
import com.w2sv.autocrop.activities.examination.fragments.adjustment.extensions.animateToTarget
import com.w2sv.autocrop.activities.examination.fragments.adjustment.extensions.clone
import com.w2sv.autocrop.activities.examination.fragments.adjustment.extensions.getEdgeTouch
import com.w2sv.autocrop.activities.examination.fragments.adjustment.extensions.getInverse
import com.w2sv.autocrop.activities.examination.fragments.adjustment.extensions.isWithinRectangle
import com.w2sv.autocrop.activities.examination.fragments.adjustment.extensions.maxRectFFrom
import com.w2sv.autocrop.activities.examination.fragments.adjustment.extensions.minRectFFrom
import com.w2sv.autocrop.activities.examination.fragments.adjustment.model.AnimatableRectF
import com.w2sv.autocrop.activities.examination.fragments.adjustment.model.DraggingState
import com.w2sv.autocrop.activities.examination.fragments.adjustment.model.DraggingState.DraggingEdge
import com.w2sv.autocrop.activities.examination.fragments.adjustment.model.Edge
import com.w2sv.autocrop.activities.examination.fragments.adjustment.model.Edge.BOTTOM
import com.w2sv.autocrop.activities.examination.fragments.adjustment.model.Edge.LEFT
import com.w2sv.autocrop.activities.examination.fragments.adjustment.model.Edge.RIGHT
import com.w2sv.autocrop.activities.examination.fragments.adjustment.model.Edge.TOP
import com.w2sv.cropbundle.cropping.CropEdges
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
     * Minimum scale limitation is dependens on screen
     * and bitmap size. bitmapMinRect is calculated
     * initially. This value holds the miminum rectangle
     * which bitmapMatrix can be.
     */
    private val bitmapMinRect: RectF by lazy {
        val bitmapMinRectSize = max(bitmapRect.width(), bitmapRect.height()) / BITMAP_MAX_SCALE
        RectF(0f, 0f, bitmapMinRectSize, bitmapMinRectSize)
    }

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
    private val bitmapRect: RectF by lazy {
        RectF(
            0f,
            0f,
            viewModel.screenshotBitmap.width.toFloat(),
            viewModel.screenshotBitmap.height.toFloat(),
        )
    }

    private val viewRect by lazy {
        RectF(0f, 0f, measuredWidth.toFloat(), measuredHeight.toFloat())
    }

    private val viewWidth: Float by lazy {
        measuredWidth.toFloat() - (CROP_RECT_MARGIN * 2)
    }
    private val viewHeight by lazy {
        measuredHeight.toFloat() - (CROP_RECT_MARGIN * 2)
    }

    /**
     * Bitmap matrix to draw bitmap on canvas
     */
    private val bitmapMatrix: Matrix = Matrix()

    private val bitmapBorderRect = RectF()

    private val viewModel by viewModel<CropAdjustmentFragment.ViewModel>()

    // ----------------------------------
    // Initialization

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        if (!isInEditMode) {
            setWillNotDraw(false)
        }
    }

    private fun initialize() {
        initializeBitmapMatrix()
        initializeCropRect()

        requestLayout()
        invalidate()
    }

    private fun initializeBitmapMatrix() {
        val scale = min(viewWidth / bitmapRect.width(), viewHeight / bitmapRect.height())
        bitmapMatrix.setScale(scale, scale)

        val translateX = (viewWidth - bitmapRect.width() * scale) / 2f + CROP_RECT_MARGIN
        val translateY = (viewHeight - bitmapRect.height() * scale) / 2f + CROP_RECT_MARGIN
        bitmapMatrix.postTranslate(translateX, translateY)

        setBitmapBorderRect()
        setViewDomainScaledEdgeCandidatePoints()
    }

    private fun initializeCropRect() {
        bitmapMatrix.mapRect(
            cropRect,
            viewModel.initialCropRectF
        )
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        initialize()
    }

    fun reset() {
        initialize()
        onCropRectChanged()
    }

    // ----------------------------------
    // Touch Events

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
                        val centerRect = getTargetRect()

                        animateBitmapTo(centerRect)
                        animateCropRectTo(centerRect)
                    }

                    else -> Unit
                }
            }
        }

        if (draggingState == DraggingState.DraggingCropRect)
            gestureDetector.onTouchEvent(event)

        invalidate()
        return true
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

    private var draggingState: DraggingState = DraggingState.Idle

    private fun setDraggingState(event: MotionEvent) {
        val edge = cropRect.getEdgeTouch(event, TOUCH_THRESHOLD)

        draggingState = when {
            edge != null -> DraggingEdge(edge)
            event.isWithinRectangle(cropRect) -> DraggingState.DraggingCropRect
            else -> DraggingState.Idle
        }
    }

    private val gestureDetector = GestureDetector(
        context,
        object : GestureDetector.SimpleOnGestureListener() {
            override fun onScroll(
                e1: MotionEvent,
                e2: MotionEvent,
                distanceX: Float,
                distanceY: Float
            ): Boolean {
                val topNew = cropRect.top - distanceY
                val bottomNew = cropRect.bottom - distanceY

                if (topNew > bitmapBorderRect.top && bottomNew < bitmapBorderRect.bottom) {
                    cropRect.top = topNew
                    cropRect.bottom = bottomNew

                    onCropRectChanged()
                    invalidate()
                }

                return true
            }
        }
    )

    // ----------------------------------
    // Drawing

    /**
     * Draw bitmap, cropRect, overlay
     */
    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        canvas?.apply {
            drawBitmap(viewModel.screenshotBitmap, bitmapMatrix, emptyPaint)
            save()
            clipRect(cropRect)
            drawColor(ContextCompat.getColor(context, R.color.crop_mask))
            restore()

            drawCropEdgeCandidates()
            drawCropRectGrid()
            drawCropRectCorners()
        }
    }

    private val emptyPaint = Paint().apply {
        isAntiAlias = true
    }

    // ----------------------------------
    // .EdgeCandidates

    private fun Canvas.drawCropEdgeCandidates() {
        drawLines(viewDomainScaledEdgeCandidatePoints, edgeCandidatePaint)
    }

    private val viewDomainScaledEdgeCandidatePoints: FloatArray by lazy {
        FloatArray(viewModel.cropBundle.screenshot.cropEdgeCandidates.size * 4)
    }

    private fun setViewDomainScaledEdgeCandidatePoints() {
        bitmapMatrix.mapPoints(viewDomainScaledEdgeCandidatePoints, viewModel.edgeCandidatePoints)
    }

    private val edgeCandidatePaint = Paint().apply {
        color = Color.CYAN
        strokeWidth = 3f
        style = Paint.Style.STROKE
    }

    // ----------------------------------
    // .CropRectGrid

    private fun Canvas.drawCropRectGrid() {

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

    private val gridPaint = Paint().apply {
        color = Color.WHITE
        strokeWidth = GRID_LINE_WIDTH
        style = Paint.Style.STROKE
    }

    private val accentPaint: Paint by lazy {
        Paint().apply {
            color = context.getColor(com.w2sv.common.R.color.magenta_saturated)
            strokeWidth = 3F
            style = Paint.Style.FILL
        }
    }

    // ----------------------------------
    // .CropRectCorners

    private fun Canvas.drawCropRectCorners() {
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

    private val cornerPaint: Paint by lazy {
        Paint().apply {
            color = context.getColor(com.w2sv.common.R.color.magenta_saturated)
            strokeWidth = 6F
            style = Paint.Style.FILL
        }
    }

    private fun setBitmapBorderRect() {
        bitmapMatrix.mapRect(bitmapBorderRect, bitmapRect)
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

    // ----------------------------------
    // CropRect Animation

    private fun getTargetRect(): AnimatableRectF {
        val heightScale = viewHeight / cropRect.height()
        val widthScale = viewWidth / cropRect.width()
        val scale = min(heightScale, widthScale)

        val targetRectWidth = cropRect.width() * scale
        val targetRectHeight = cropRect.height() * scale

        val targetRectLeft = (viewWidth - targetRectWidth) / 2f + CROP_RECT_MARGIN
        val targetRectTop = (viewHeight - targetRectHeight) / 2f + CROP_RECT_MARGIN
        val targetRectRight = targetRectLeft + targetRectWidth
        val targetRectBottom = targetRectTop + targetRectHeight

        return AnimatableRectF(targetRectLeft, targetRectTop, targetRectRight, targetRectBottom)
    }

    private fun animateBitmapTo(dst: AnimatableRectF) {
        val newBitmapMatrix = bitmapMatrix.clone()

        val scale = dst.width() / cropRect.width()
        val translateX = dst.centerX() - cropRect.centerX()
        val translateY = dst.centerY() - cropRect.centerY()

        val matrix = Matrix()
        matrix.setScale(scale, scale, cropRect.centerX(), cropRect.centerY())
        matrix.postTranslate(translateX, translateY)
        newBitmapMatrix.postConcat(matrix)

        bitmapMatrix.animateToTarget(newBitmapMatrix) {
            setViewDomainScaledEdgeCandidatePoints()
            invalidate()
        }
    }

    private fun animateCropRectTo(dst: AnimatableRectF) {
        cropRect.animateTo(dst, ANIMATION_DURATION) {
            invalidate()
        }
    }

    // ----------------------------------
    // Value Exposition

    private fun onCropRectChanged() {
        viewModel.cropEdgesLive.postValue(
            getImageDomainCropRectF().run {
                CropEdges(top.roundToInt(), bottom.roundToInt())
            }
        )
    }

    private fun getImageDomainCropRectF(): RectF =
        RectF()
            .apply {
                bitmapMatrix.getInverse().mapRect(this, cropRect)
            }

    companion object {
        private const val BITMAP_MAX_SCALE = 15f

        private const val TOUCH_THRESHOLD: Float = 32f

        private const val CROP_RECT_MARGIN: Float = 24f

        private const val GRID_LINE_WIDTH: Float = 1f

        private const val CORNER_WIDTH: Float = 3f

        private const val CORNER_EDGE_LENGTH: Int = 16

        private const val MIN_RECT_SIZE: Float = 56f

        private const val ANIMATION_DURATION: Long = 300
    }
}