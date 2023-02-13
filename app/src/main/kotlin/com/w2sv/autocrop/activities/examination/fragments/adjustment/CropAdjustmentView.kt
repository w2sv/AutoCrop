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
import com.w2sv.autocrop.activities.examination.fragments.adjustment.extensions.animateMatrix
import com.w2sv.autocrop.activities.examination.fragments.adjustment.extensions.clone
import com.w2sv.autocrop.activities.examination.fragments.adjustment.extensions.getEdgeTouch
import com.w2sv.autocrop.activities.examination.fragments.adjustment.extensions.getInverse
import com.w2sv.autocrop.activities.examination.fragments.adjustment.extensions.getRectF
import com.w2sv.autocrop.activities.examination.fragments.adjustment.extensions.isWithinRectangle
import com.w2sv.autocrop.activities.examination.fragments.adjustment.extensions.maxRectOf
import com.w2sv.autocrop.activities.examination.fragments.adjustment.extensions.minRectOf
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

    private val viewModel by viewModel<CropAdjustmentFragment.ViewModel>()

    // ----------------------------------
    // View

    private val viewRectF by lazy {
        RectF(0f, 0f, measuredWidth.toFloat(), measuredHeight.toFloat())
    }

    private val viewWidth: Float by lazy {
        measuredWidth.toFloat() - (CROP_RECT_MARGIN * 2)
    }
    private val viewHeight by lazy {
        measuredHeight.toFloat() - (CROP_RECT_MARGIN * 2)
    }

    // ----------------------------------
    // Image

    private val imageRectF: RectF by lazy {
        viewModel.screenshotBitmap.getRectF()
    }

    /**
     * public var required for working of [animateMatrix]
     */
    var imageMatrix: Matrix = Matrix()

    private lateinit var defaultImageMatrix: Matrix

    private val imageBorderRectFViewDomain = RectF()

    private val imageMinRectF: RectF by lazy {
        val bitmapMinRectSize = max(imageRectF.width(), imageRectF.height()) / BITMAP_MAX_SCALE
        RectF(0f, 0f, bitmapMinRectSize, bitmapMinRectSize)
    }

    // ----------------------------------
    // CropRect

    private val cropRect: AnimatableRectF =
        AnimatableRectF()

    private lateinit var defaultCropRectF: RectF

    private val minCropRectF = RectF()
    private val maxCropRectF = RectF()

    // ----------------------------------
    // Initialization

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        if (!isInEditMode) {
            setWillNotDraw(false)
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        initialize()
    }

    private fun initialize() {
        setImageMatrix()
        defaultImageMatrix = imageMatrix.clone()

        setImageBorderRectFViewDomain()
        setViewDomainScaledEdgeCandidatePoints()
        setCropRect()
        defaultCropRectF = RectF(cropRect.toRectF())

        requestLayout()
        invalidate()
    }

    private fun setImageMatrix() {
        val scale = min(viewWidth / imageRectF.width(), viewHeight / imageRectF.height())
        imageMatrix.setScale(scale, scale)

        val translateX = (viewWidth - imageRectF.width() * scale) / 2f + CROP_RECT_MARGIN
        val translateY = (viewHeight - imageRectF.height() * scale) / 2f + CROP_RECT_MARGIN
        imageMatrix.postTranslate(translateX, translateY)
    }

    private fun setCropRect() {
        imageMatrix.mapRect(
            cropRect,
            viewModel.initialCropRectF
        )
    }

    private fun setImageBorderRectFViewDomain() {
        imageMatrix.mapRect(imageBorderRectFViewDomain, imageRectF)
    }

    fun reset() {
        animateImageTo(defaultImageMatrix)
        animateCropRectTo(defaultCropRectF)
        setImageBorderRectFViewDomain()
        viewModel.resetCropEdges()
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
                    setImageBorderRectFViewDomain()

                    setMinCropRectF()
                    setMaxCropRectF()
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
                minCropRectF.setEmpty()
                maxCropRectF.setEmpty()
                when (draggingState) {
                    is DraggingEdge, is DraggingState.DraggingCropRect -> {
                        val centerRect = getCenteredViewDomainCropRect()

                        animateImageTo(centerRect)
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

                if (topNew > imageBorderRectFViewDomain.top && bottomNew < imageBorderRectFViewDomain.bottom) {
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
            drawBitmap(viewModel.screenshotBitmap, imageMatrix, null)

            when (viewModel.modeLive) {
                CropAdjustmentMode.EdgeSelection -> drawCropEdgeCandidates()
                CropAdjustmentMode.Manual -> drawCropRect()
            }
        }
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
        imageMatrix.mapPoints(viewDomainScaledEdgeCandidatePoints, viewModel.edgeCandidatePoints)
    }

    private val edgeCandidatePaint = Paint().apply {
        color = Color.CYAN
        strokeWidth = 3f
        style = Paint.Style.STROKE
    }

    // ----------------------------------
    // .CropRect

    private fun Canvas.drawCropRect() {
        save()
        clipRect(cropRect)
        drawColor(ContextCompat.getColor(context, R.color.crop_mask))
        restore()

        drawCropRectGrid()
    }

    private fun Canvas.drawCropRectGrid() {

        // -------------
        // Outer rectangle

        // Vertical edges

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

        // Horizontal edges

        drawLine(
            cropRect.left,
            cropRect.top,
            cropRect.right,
            cropRect.top,
            horizontalCropRectEdgePaint
        )

        drawLine(
            cropRect.left,
            cropRect.bottom,
            cropRect.right,
            cropRect.bottom,
            horizontalCropRectEdgePaint
        )

        // Horizontally centered protrusions

        drawLine(
            cropRect.centerX() - DELTA_CENTER_HORIZONTAL_EDGE_PROTRUSION,
            cropRect.top,
            cropRect.centerX() + DELTA_CENTER_HORIZONTAL_EDGE_PROTRUSION,
            cropRect.top,
            horizontalProtrusionPaint
        )

        drawLine(
            cropRect.centerX() - DELTA_CENTER_HORIZONTAL_EDGE_PROTRUSION,
            cropRect.bottom,
            cropRect.centerX() + DELTA_CENTER_HORIZONTAL_EDGE_PROTRUSION,
            cropRect.bottom,
            horizontalProtrusionPaint
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
        strokeWidth = 1F
        style = Paint.Style.STROKE
    }

    private val horizontalCropRectEdgePaint: Paint by lazy {
        Paint().apply {
            color = context.getColor(com.w2sv.common.R.color.magenta_saturated)
            strokeWidth = 3F
            style = Paint.Style.FILL
        }
    }

    private val horizontalProtrusionPaint: Paint by lazy {
        Paint().apply {
            color = context.getColor(com.w2sv.common.R.color.magenta_saturated)
            strokeWidth = 8F
            style = Paint.Style.FILL
        }
    }

    private fun setMinCropRectF() {
        val minSize = max(
            RectF()
                .apply { imageMatrix.mapRect(this, imageMinRectF) }
                .width(),
            MIN_RECT_SIZE
        )

        when (val state = draggingState) {
            is DraggingEdge -> {
                when (state.edge) {
                    LEFT -> minCropRectF.set(
                        cropRect.right - minSize,
                        cropRect.top,
                        cropRect.right,
                        cropRect.bottom
                    )

                    TOP -> minCropRectF.set(
                        cropRect.left,
                        cropRect.bottom - minSize,
                        cropRect.right,
                        cropRect.bottom
                    )

                    RIGHT -> minCropRectF.set(
                        cropRect.left,
                        cropRect.top,
                        cropRect.left + minSize,
                        cropRect.bottom
                    )

                    BOTTOM -> minCropRectF.set(
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

    private fun setMaxCropRectF() {
        val borderRect = maxRectOf(imageBorderRectFViewDomain, viewRectF)

        when (val state = draggingState) {
            is DraggingEdge -> {
                when (state.edge) {
                    LEFT -> maxCropRectF.set(
                        borderRect.left,
                        cropRect.top,
                        cropRect.right,
                        cropRect.bottom
                    )

                    TOP -> maxCropRectF.set(
                        cropRect.left,
                        borderRect.top,
                        cropRect.right,
                        cropRect.bottom
                    )

                    RIGHT -> maxCropRectF.set(
                        cropRect.left,
                        cropRect.top,
                        borderRect.right,
                        cropRect.bottom
                    )

                    BOTTOM -> maxCropRectF.set(
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
        cropRect.set(maxRectOf(cropRect, maxCropRectF))
    }

    private fun updateExceedMinBorders() {
        cropRect.set(minRectOf(cropRect, minCropRectF))
    }

    // ----------------------------------
    // CropRect Animation

    private fun getCenteredViewDomainCropRect(): RectF {
        val heightScale = viewHeight / cropRect.height()
        val widthScale = viewWidth / cropRect.width()
        val scale = min(heightScale, widthScale)

        val targetRectWidth = cropRect.width() * scale
        val targetRectHeight = cropRect.height() * scale

        val targetRectLeft = (viewWidth - targetRectWidth) / 2f + CROP_RECT_MARGIN
        val targetRectTop = (viewHeight - targetRectHeight) / 2f + CROP_RECT_MARGIN
        val targetRectRight = targetRectLeft + targetRectWidth
        val targetRectBottom = targetRectTop + targetRectHeight

        return RectF(targetRectLeft, targetRectTop, targetRectRight, targetRectBottom)
    }

    private fun animateImageTo(dst: RectF) {
        val newBitmapMatrix = imageMatrix.clone()

        val scale = dst.width() / cropRect.width()
        val translateX = dst.centerX() - cropRect.centerX()
        val translateY = dst.centerY() - cropRect.centerY()

        val matrix = Matrix()
        matrix.setScale(scale, scale, cropRect.centerX(), cropRect.centerY())
        matrix.postTranslate(translateX, translateY)
        newBitmapMatrix.postConcat(matrix)

        animateImageTo(newBitmapMatrix)
    }

    private fun animateImageTo(dst: Matrix) {
        animateMatrix(this, "imageMatrix", imageMatrix, dst, ANIMATION_DURATION)
        //        imageMatrix.animateToTarget(dst, ANIMATION_DURATION) {
        //            setViewDomainScaledEdgeCandidatePoints()
        //            invalidate()
        //        }
    }

    private fun animateCropRectTo(dst: RectF) {
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
                imageMatrix.getInverse().mapRect(this, cropRect)
            }

    companion object {
        private const val BITMAP_MAX_SCALE = 15f

        private const val TOUCH_THRESHOLD: Float = 32f

        private const val CROP_RECT_MARGIN: Float = 32f

        private const val MIN_RECT_SIZE: Float = 56f

        private const val ANIMATION_DURATION: Long = 300

        private const val DELTA_CENTER_HORIZONTAL_EDGE_PROTRUSION: Float = 32f
    }
}