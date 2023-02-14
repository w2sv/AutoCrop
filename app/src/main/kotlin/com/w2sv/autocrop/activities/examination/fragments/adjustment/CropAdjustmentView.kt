package com.w2sv.autocrop.activities.examination.fragments.adjustment

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.BlurMaskFilter
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
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.findViewTreeLifecycleOwner
import com.w2sv.androidutils.extensions.postValue
import com.w2sv.androidutils.extensions.viewModel
import com.w2sv.autocrop.R
import com.w2sv.autocrop.activities.examination.fragments.adjustment.extensions.animateMatrix
import com.w2sv.autocrop.activities.examination.fragments.adjustment.extensions.clone
import com.w2sv.autocrop.activities.examination.fragments.adjustment.extensions.contains
import com.w2sv.autocrop.activities.examination.fragments.adjustment.extensions.containsVerticalEdges
import com.w2sv.autocrop.activities.examination.fragments.adjustment.extensions.getEdgeTouch
import com.w2sv.autocrop.activities.examination.fragments.adjustment.extensions.getInverse
import com.w2sv.autocrop.activities.examination.fragments.adjustment.extensions.getRectF
import com.w2sv.autocrop.activities.examination.fragments.adjustment.extensions.maxRectOf
import com.w2sv.autocrop.activities.examination.fragments.adjustment.extensions.minRectOf
import com.w2sv.autocrop.activities.examination.fragments.adjustment.extensions.onHorizontalLine
import com.w2sv.autocrop.activities.examination.fragments.adjustment.extensions.setVerticalEdges
import com.w2sv.autocrop.activities.examination.fragments.adjustment.model.AnimatableRectF
import com.w2sv.autocrop.activities.examination.fragments.adjustment.model.DraggingState
import com.w2sv.autocrop.activities.examination.fragments.adjustment.model.Edge
import com.w2sv.autocrop.activities.examination.fragments.adjustment.model.Edge.BOTTOM
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

    private val imageRectImageDomain: RectF by lazy {
        viewModel.screenshotBitmap.getRectF()
    }

    /**
     * public var required for proper working of [animateMatrix]
     */
    var imageMatrix: Matrix = Matrix()

    private lateinit var defaultImageMatrix: Matrix

    private val imageBorderRectViewDomain = RectF()

    private val imageMinRectViewDomain: RectF by lazy {
        val bitmapMinRectSize = max(imageRectImageDomain.width(), imageRectImageDomain.height()) / BITMAP_MAX_SCALE
        RectF(0f, 0f, bitmapMinRectSize, bitmapMinRectSize)
    }

    // ----------------------------------
    // CropRect

    private val cropRectViewDomain: AnimatableRectF =
        AnimatableRectF()

    private lateinit var defaultCropRectViewDomain: RectF

    private val minCropRectViewDomain = RectF()
    private val maxCropRectViewDomain = RectF()

    // ----------------------------------
    // Initialization

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        if (!isInEditMode) {
            setWillNotDraw(false)

            viewModel.setLiveDataObservers(findViewTreeLifecycleOwner()!!)
        }
    }

    private fun CropAdjustmentFragment.ViewModel.setLiveDataObservers(owner: LifecycleOwner) {
        selectedStartEdgeIndex.observe(owner) {
            invalidate()
        }
        selectedEndEdgeIndex.observe(owner) {
            if (it != null) {
                val sortedVerticalEdges = listOf(
                    edgeCandidateYsViewDomain[it],
                    edgeCandidateYsViewDomain[selectedStartEdgeIndex.value!!]
                )
                    .sorted()

                cropRectViewDomain.setVerticalEdges(
                    sortedVerticalEdges[0],
                    sortedVerticalEdges[1]
                )
            }

            invalidate()
        }
        resetModeLive.observe(owner) {
            reset(it)
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
        setCropRect()
        defaultCropRectViewDomain = RectF(cropRectViewDomain.toRectF())

        requestLayout()
        invalidate()
    }

    private fun setImageMatrix() {
        val scale = min(viewWidth / imageRectImageDomain.width(), viewHeight / imageRectImageDomain.height())
        imageMatrix.setScale(scale, scale)

        val translateX = (viewWidth - imageRectImageDomain.width() * scale) / 2f + CROP_RECT_MARGIN
        val translateY = (viewHeight - imageRectImageDomain.height() * scale) / 2f + CROP_RECT_MARGIN
        imageMatrix.postTranslate(translateX, translateY)
    }

    private fun setCropRect() {
        imageMatrix.mapRect(
            cropRectViewDomain,
            viewModel.initialCropRectF
        )
    }

    private fun setImageBorderRectFViewDomain() {
        imageMatrix.mapRect(imageBorderRectViewDomain, imageRectImageDomain)
    }

    fun reset(mode: CropAdjustmentMode) {
        when (mode) {
            CropAdjustmentMode.Manual -> {
                viewModel.drawMode = CropAdjustmentMode.Manual
                animateImageTo(defaultImageMatrix)
                animateCropRectTo(defaultCropRectViewDomain)
                setImageBorderRectFViewDomain()
            }

            CropAdjustmentMode.EdgeSelection -> {
                animateImageTo(defaultImageMatrix) {
                    setImageBorderRectFViewDomain()

                    ::edgeCandidatePointsViewDomain.get()
                    ::edgeCandidateYsViewDomain.get()

                    viewModel.drawMode = CropAdjustmentMode.EdgeSelection

                    requestLayout()
                    invalidate()
                }
            }
        }

        viewModel.resetCropEdges()
    }

    // ----------------------------------
    // Touch Events

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event == null || viewModel.drawMode == null)
            return false

        when (viewModel.drawMode!!) {
            CropAdjustmentMode.Manual -> onManualAdjustmentModeTouchEvent(event)
            CropAdjustmentMode.EdgeSelection -> onEdgeSelectionModeTouchEvent(event)
        }

        return true
    }

    private fun onManualAdjustmentModeTouchEvent(event: MotionEvent) {
        when (event.action) {
            ACTION_DOWN -> {
                setDraggingState(event)

                if (draggingState !is DraggingState.Idle) {
                    setImageBorderRectFViewDomain()
                }
                if (draggingState is DraggingState.DraggingEdge) {
                    val edge = (draggingState as DraggingState.DraggingEdge).edge
                    setMinCropRectF(edge)
                    setMaxCropRectF(edge)
                }
            }

            ACTION_MOVE -> {
                when (draggingState) {
                    is DraggingState.DraggingEdge -> {
                        when ((draggingState as DraggingState.DraggingEdge).edge) {
                            TOP -> cropRectViewDomain.top = event.y
                            BOTTOM -> cropRectViewDomain.bottom = event.y
                        }
                        cropRectViewDomain.set(maxRectOf(cropRectViewDomain, maxCropRectViewDomain))
                        cropRectViewDomain.set(minRectOf(cropRectViewDomain, minCropRectViewDomain))
                        onCropRectChanged()
                        invalidate()
                    }

                    else -> Unit
                }
            }

            ACTION_UP -> {
                minCropRectViewDomain.setEmpty()
                maxCropRectViewDomain.setEmpty()

                when (draggingState) {
                    is DraggingState.DraggingEdge, is DraggingState.DraggingCropRect -> {
                        val centerRect = getCenteredViewDomainCropRect()

                        animateImageTo(centerRect)
                        animateCropRectTo(centerRect)

                        invalidate()
                    }

                    else -> Unit
                }
            }
        }

        if (draggingState == DraggingState.DraggingCropRect) {
            gestureDetector.onTouchEvent(event)
            invalidate()
        }
    }

    private fun onEdgeSelectionModeTouchEvent(event: MotionEvent) {
        if (event.action == ACTION_DOWN && imageBorderRectViewDomain.contains(event))
            edgeCandidateYsViewDomain.forEachIndexed { selectedEdgeCandidateIndex, y ->
                if (event.onHorizontalLine(y, TOUCH_THRESHOLD)) {
                    when (viewModel.selectedStartEdgeIndex.value) {
                        null -> {
                            viewModel.selectedStartEdgeIndex.postValue(selectedEdgeCandidateIndex)
                            viewModel.selectedEndEdgeIndex.postValue(null)
                        }

                        selectedEdgeCandidateIndex -> {
                            viewModel.selectedStartEdgeIndex.postValue(null)
                            viewModel.selectedEndEdgeIndex.postValue(null)
                        }

                        else -> viewModel.selectedEndEdgeIndex.postValue(selectedEdgeCandidateIndex)
                    }
                }
            }
    }

    private var draggingState: DraggingState = DraggingState.Idle

    private fun setDraggingState(event: MotionEvent) {
        val edge = cropRectViewDomain.getEdgeTouch(event, TOUCH_THRESHOLD)

        draggingState = when {
            edge != null -> DraggingState.DraggingEdge(edge)
            cropRectViewDomain.contains(event) -> DraggingState.DraggingCropRect
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
                val y1 = cropRectViewDomain.top - distanceY
                val y2 = cropRectViewDomain.bottom - distanceY

                if (imageBorderRectViewDomain.containsVerticalEdges(y1, y2)) {
                    cropRectViewDomain.setVerticalEdges(y1, y2)
                    onCropRectChanged()
                    return true
                }
                return false
            }
        }
    )

    // ----------------------------------
    // Drawing

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        canvas?.apply {
            drawBitmap(viewModel.screenshotBitmap, imageMatrix, null)

            when (viewModel.drawMode) {
                CropAdjustmentMode.EdgeSelection -> drawEdgeSelectionMode()
                CropAdjustmentMode.Manual -> drawManualMode()
                else -> Unit
            }
        }
    }

    // ----------------------------------
    // .EdgeCandidates

    private fun Canvas.drawEdgeSelectionMode() {
        drawLines(edgeCandidatePointsViewDomain, edgeCandidatePaint)

        with(viewModel) {
            selectedStartEdgeIndex.value?.let {
                drawSelectedEdgeHighlighting(it)
            }
            selectedEndEdgeIndex.value?.let {
                drawSelectedEdgeHighlighting(it)
            }
            if (selectedStartEdgeIndex.value != null && selectedEndEdgeIndex.value != null) {
                drawCropMask()
            }
        }
    }

    private fun Canvas.drawSelectedEdgeHighlighting(selectedEdgeIndex: Int) {
        drawLines(
            edgeCandidatePointsViewDomain,
            selectedEdgeIndex * COORDINATES_PER_LINE,
            COORDINATES_PER_LINE,
            selectedEdgeCandidatePaint
        )
    }

    private val selectedEdgeCandidatePaint = Paint().apply {
        color = Color.CYAN
        strokeWidth = 9f
        style = Paint.Style.STROKE
        maskFilter = BlurMaskFilter(8f, BlurMaskFilter.Blur.NORMAL)
    }

    private val edgeCandidatePaint = Paint().apply {
        color = Color.CYAN
        strokeWidth = 3f
        style = Paint.Style.STROKE
    }

    private val edgeCandidatePointsViewDomain: FloatArray by lazy {
        FloatArray(viewModel.cropBundle.screenshot.cropEdgeCandidates.size * COORDINATES_PER_LINE).apply {
            imageMatrix.mapPoints(this, viewModel.edgeCandidatePoints)
        }
    }

    private val edgeCandidateYsViewDomain: List<Float> by lazy {
        mutableListOf<Float>().apply {
            (1 until edgeCandidatePointsViewDomain.size step COORDINATES_PER_LINE).forEach {
                add(edgeCandidatePointsViewDomain[it])
            }
        }
    }

    // ----------------------------------
    // .CropRect

    private fun Canvas.drawManualMode() {
        drawCropMask()
        drawCropRectGrid()
    }

    private fun Canvas.drawCropMask() {
        save()
        clipRect(cropRectViewDomain)
        drawColor(ContextCompat.getColor(context, R.color.crop_mask))
        restore()
    }

    private fun Canvas.drawCropRectGrid() {

        // -------------
        // Outer rectangle

        // Vertical edges

        drawLine(
            cropRectViewDomain.left,
            cropRectViewDomain.bottom,
            cropRectViewDomain.left,
            cropRectViewDomain.top,
            gridPaint
        )

        drawLine(
            cropRectViewDomain.right,
            cropRectViewDomain.bottom,
            cropRectViewDomain.right,
            cropRectViewDomain.top,
            gridPaint
        )

        // Horizontal edges

        drawLine(
            cropRectViewDomain.left,
            cropRectViewDomain.top,
            cropRectViewDomain.right,
            cropRectViewDomain.top,
            horizontalCropRectEdgePaint
        )

        drawLine(
            cropRectViewDomain.left,
            cropRectViewDomain.bottom,
            cropRectViewDomain.right,
            cropRectViewDomain.bottom,
            horizontalCropRectEdgePaint
        )

        // Horizontally centered protrusions

        drawLine(
            cropRectViewDomain.centerX() - DELTA_CENTER_HORIZONTAL_EDGE_PROTRUSION,
            cropRectViewDomain.top,
            cropRectViewDomain.centerX() + DELTA_CENTER_HORIZONTAL_EDGE_PROTRUSION,
            cropRectViewDomain.top,
            horizontalProtrusionPaint
        )

        drawLine(
            cropRectViewDomain.centerX() - DELTA_CENTER_HORIZONTAL_EDGE_PROTRUSION,
            cropRectViewDomain.bottom,
            cropRectViewDomain.centerX() + DELTA_CENTER_HORIZONTAL_EDGE_PROTRUSION,
            cropRectViewDomain.bottom,
            horizontalProtrusionPaint
        )

        /**
         * Inner lines
         */

        drawLine(
            cropRectViewDomain.left + cropRectViewDomain.width() / 3f,
            cropRectViewDomain.top,
            cropRectViewDomain.left + cropRectViewDomain.width() / 3f,
            cropRectViewDomain.bottom,
            gridPaint
        )

        drawLine(
            cropRectViewDomain.left + cropRectViewDomain.width() * 2f / 3f,
            cropRectViewDomain.top,
            cropRectViewDomain.left + cropRectViewDomain.width() * 2f / 3f,
            cropRectViewDomain.bottom,
            gridPaint
        )

        drawLine(
            cropRectViewDomain.left,
            cropRectViewDomain.top + cropRectViewDomain.height() / 3f,
            cropRectViewDomain.right,
            cropRectViewDomain.top + cropRectViewDomain.height() / 3f,
            gridPaint
        )

        drawLine(
            cropRectViewDomain.left,
            cropRectViewDomain.top + cropRectViewDomain.height() * 2f / 3f,
            cropRectViewDomain.right,
            cropRectViewDomain.top + cropRectViewDomain.height() * 2f / 3f,
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

    private fun setMinCropRectF(draggedEdge: Edge) {
        val minSize = max(
            RectF()
                .apply { imageMatrix.mapRect(this, imageMinRectViewDomain) }
                .width(),
            MIN_RECT_SIZE
        )

        when (draggedEdge) {
            TOP -> minCropRectViewDomain.set(
                cropRectViewDomain.left,
                cropRectViewDomain.bottom - minSize,
                cropRectViewDomain.right,
                cropRectViewDomain.bottom
            )

            BOTTOM -> minCropRectViewDomain.set(
                cropRectViewDomain.left,
                cropRectViewDomain.top,
                cropRectViewDomain.right,
                cropRectViewDomain.top + minSize
            )
        }
    }

    private fun setMaxCropRectF(draggedEdge: Edge) {
        val borderRect = maxRectOf(imageBorderRectViewDomain, viewRectF)

        when (draggedEdge) {
            TOP -> maxCropRectViewDomain.set(
                cropRectViewDomain.left,
                borderRect.top,
                cropRectViewDomain.right,
                cropRectViewDomain.bottom
            )

            BOTTOM -> maxCropRectViewDomain.set(
                cropRectViewDomain.left,
                cropRectViewDomain.top,
                cropRectViewDomain.right,
                borderRect.bottom
            )
        }

    }

    // ----------------------------------
    // CropRect Animation

    private fun getCenteredViewDomainCropRect(): RectF {
        val heightScale = viewHeight / cropRectViewDomain.height()
        val widthScale = viewWidth / cropRectViewDomain.width()
        val scale = min(heightScale, widthScale)

        val targetRectWidth = cropRectViewDomain.width() * scale
        val targetRectHeight = cropRectViewDomain.height() * scale

        val targetRectLeft = (viewWidth - targetRectWidth) / 2f + CROP_RECT_MARGIN
        val targetRectTop = (viewHeight - targetRectHeight) / 2f + CROP_RECT_MARGIN
        val targetRectRight = targetRectLeft + targetRectWidth
        val targetRectBottom = targetRectTop + targetRectHeight

        return RectF(targetRectLeft, targetRectTop, targetRectRight, targetRectBottom)
    }

    private fun animateImageTo(dst: RectF) {
        val newBitmapMatrix = imageMatrix.clone()

        val scale = dst.width() / cropRectViewDomain.width()
        val translateX = dst.centerX() - cropRectViewDomain.centerX()
        val translateY = dst.centerY() - cropRectViewDomain.centerY()

        val matrix = Matrix()
        matrix.setScale(scale, scale, cropRectViewDomain.centerX(), cropRectViewDomain.centerY())
        matrix.postTranslate(translateX, translateY)
        newBitmapMatrix.postConcat(matrix)

        animateImageTo(newBitmapMatrix)
    }

    private fun animateImageTo(dst: Matrix, onEnd: (() -> Unit)? = null) {
        animateMatrix(this, "imageMatrix", imageMatrix, dst, ANIMATION_DURATION, onEnd)
    }

    private fun animateCropRectTo(dst: RectF) {
        cropRectViewDomain.animateTo(dst, ANIMATION_DURATION) {
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
                imageMatrix.getInverse().mapRect(this, cropRectViewDomain)
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

private const val COORDINATES_PER_LINE = 4