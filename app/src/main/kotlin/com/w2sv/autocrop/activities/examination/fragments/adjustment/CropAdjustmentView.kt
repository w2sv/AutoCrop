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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.findViewTreeLifecycleOwner
import com.w2sv.androidutils.extensions.postValue
import com.w2sv.androidutils.extensions.viewModel
import com.w2sv.autocrop.R
import com.w2sv.autocrop.activities.examination.fragments.adjustment.extensions.animateMatrix
import com.w2sv.autocrop.activities.examination.fragments.adjustment.extensions.contains
import com.w2sv.autocrop.activities.examination.fragments.adjustment.extensions.containsVerticalEdges
import com.w2sv.autocrop.activities.examination.fragments.adjustment.extensions.getCopy
import com.w2sv.autocrop.activities.examination.fragments.adjustment.extensions.getEdgeTouch
import com.w2sv.autocrop.activities.examination.fragments.adjustment.extensions.getInverse
import com.w2sv.autocrop.activities.examination.fragments.adjustment.extensions.maxRectOf
import com.w2sv.autocrop.activities.examination.fragments.adjustment.extensions.minRectOf
import com.w2sv.autocrop.activities.examination.fragments.adjustment.extensions.onHorizontalLine
import com.w2sv.autocrop.activities.examination.fragments.adjustment.extensions.setAsMappedFrom
import com.w2sv.autocrop.activities.examination.fragments.adjustment.extensions.setVerticalEdges
import com.w2sv.autocrop.activities.examination.fragments.adjustment.model.AnimatableRectF
import com.w2sv.autocrop.activities.examination.fragments.adjustment.model.DraggingState
import com.w2sv.autocrop.activities.examination.fragments.adjustment.model.Edge
import com.w2sv.autocrop.activities.examination.fragments.adjustment.model.Edge.BOTTOM
import com.w2sv.autocrop.activities.examination.fragments.adjustment.model.Edge.TOP
import com.w2sv.cropbundle.cropping.CropEdges
import slimber.log.i
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

private interface ModeConfig {
    fun setUp(onViewCreation: Boolean)
    fun reset() {}
    fun onTouchEvent(event: MotionEvent): Boolean
    fun onDraw(canvas: Canvas)
}

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

    /**
     * public var required for proper working of [animateMatrix]
     */
    lateinit var imageMatrix: Matrix

    private lateinit var defaultImageMatrix: Matrix

    private val imageBorderRectViewDomain = RectF()

    private val imageMinRectViewDomain: RectF by lazy {
        val bitmapMinRectSize = max(viewModel.imageRect.width(), viewModel.imageRect.height()) / BITMAP_MAX_SCALE
        RectF(0f, 0f, bitmapMinRectSize, bitmapMinRectSize)
    }

    // ----------------------------------
    // CropRect

    private val cropRectViewDomain: AnimatableRectF =
        AnimatableRectF()

    private lateinit var defaultCropRectViewDomain: RectF

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

        postLayoutInit()
    }

    private fun postLayoutInit() {
        imageMatrix = computeImageMatrix()
        defaultImageMatrix = imageMatrix.getCopy()

        cropRectViewDomain.setAsMappedFrom(viewModel.initialCropRectF, imageMatrix)
        defaultCropRectViewDomain = cropRectViewDomain.getCopy()

        viewModel.modeLive.observe(findViewTreeLifecycleOwner()!!) {
            i { "Setting modeConfig to ${it.name}" }
            setModeConfig(it)
        }
    }

    private fun setModeConfig(mode: CropAdjustmentMode) {
        modeConfig = when (mode) {
            CropAdjustmentMode.Manual -> manualModeConfig
            CropAdjustmentMode.EdgeSelection -> edgeSelectionModeConfig
        }
            .apply {
                setUp(onViewCreation = !findViewTreeLifecycleOwner()!!.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED))
            }
    }

    fun reset() {
        modeConfig.reset()
    }

    private lateinit var modeConfig: ModeConfig

    private val manualModeConfig by lazy {
        ManualModeConfig()
    }

    private val edgeSelectionModeConfig by lazy {
        EdgeSelectionModeConfig()
    }

    private fun computeImageMatrix(): Matrix =
        Matrix()
            .apply {
                val scale = min(viewWidth / viewModel.imageRect.width(), viewHeight / viewModel.imageRect.height())
                setScale(scale, scale)

                val translateX = (viewWidth - viewModel.imageRect.width() * scale) / 2f + CROP_RECT_MARGIN
                val translateY = (viewHeight - viewModel.imageRect.height() * scale) / 2f + CROP_RECT_MARGIN
                postTranslate(translateX, translateY)
            }

    private fun resetImageBorderRectViewDomain() {
        imageBorderRectViewDomain.setAsMappedFrom(viewModel.imageRect, imageMatrix)
    }

    // ----------------------------------
    // Touch Events

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean =
        when (event) {
            null -> false
            else -> modeConfig.onTouchEvent(event)
        }

    // ----------------------------------
    // Drawing

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        canvas?.apply {
            drawBitmap(viewModel.screenshotBitmap, imageMatrix, null)

            modeConfig.onDraw(this)
        }
    }

    private fun Canvas.drawCropMask() {
        save()
        clipRect(cropRectViewDomain)
        drawColor(ContextCompat.getColor(context, R.color.crop_mask))
        restore()
    }

    // ----------------------------------
    // Animation

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
            getCropRectImageDomain().run {
                CropEdges(top.roundToInt(), bottom.roundToInt())
            }
        )
    }

    private fun getCropRectImageDomain(): RectF =
        RectF()
            .apply {
                setAsMappedFrom(cropRectViewDomain, imageMatrix.getInverse())
            }

    // ----------------------------------
    // ModeConfigs

    private inner class ManualModeConfig : ModeConfig {
        private val minCropRectViewDomain = RectF()
        private val maxCropRectViewDomain = RectF()

        override fun setUp(onViewCreation: Boolean) {
            when(onViewCreation){
                true -> resetImageBorderRectViewDomain()
                false -> invalidate()
            }
        }

        override fun reset() {
            animateImageTo(defaultImageMatrix)
            animateCropRectTo(defaultCropRectViewDomain)
            resetImageBorderRectViewDomain()
        }

        override fun onTouchEvent(event: MotionEvent): Boolean {
            when (event.action) {
                ACTION_DOWN -> {
                    setDraggingState(event)

                    if (draggingState !is DraggingState.Idle) {
                        resetImageBorderRectViewDomain()
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

            return true  // TODO
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

        override fun onDraw(canvas: Canvas) {
            canvas.drawCropMask()
            canvas.drawCropRectGrid()
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
                    .apply {
                        setAsMappedFrom(imageMinRectViewDomain, imageMatrix)
                    }
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
            val newBitmapMatrix = imageMatrix.getCopy()

            val scale = dst.width() / cropRectViewDomain.width()
            val translateX = dst.centerX() - cropRectViewDomain.centerX()
            val translateY = dst.centerY() - cropRectViewDomain.centerY()

            val matrix = Matrix()
            matrix.setScale(scale, scale, cropRectViewDomain.centerX(), cropRectViewDomain.centerY())
            matrix.postTranslate(translateX, translateY)
            newBitmapMatrix.postConcat(matrix)

            animateImageTo(newBitmapMatrix)
        }
    }

    private inner class EdgeSelectionModeConfig : ModeConfig {

        private var isSetUp: Boolean = false

        init {
            viewModel.selectedEdgeCandidateIndices.observe(findViewTreeLifecycleOwner()!!) { indices ->
                if (indices?.second != null){
                    setSelectedCropEdges(indices.first, indices.second!!)
                    onCropRectChanged()
                }
                else
                    viewModel.cropEdgesLive.postValue(null)

                invalidate()
            }
        }

        private fun setSelectedCropEdges(indexFirst: Int, indexSecond: Int) {
            val sortedVerticalEdges = mutableListOf(
                edgeCandidateYsViewDomain[indexFirst],
                edgeCandidateYsViewDomain[indexSecond]
            )
                .apply {
                    sort()
                }

            cropRectViewDomain.setVerticalEdges(
                sortedVerticalEdges[0],
                sortedVerticalEdges[1]
            )
        }

        override fun setUp(onViewCreation: Boolean) {
            viewModel.selectedEdgeCandidateIndices.postValue(null)
            isSetUp = false

            when (onViewCreation) {
                false -> animateImageTo(defaultImageMatrix) {
                    onImageSet()
                }

                true -> onImageSet()
            }
        }

        private fun onImageSet() {
            resetImageBorderRectViewDomain()

            isSetUp = true

            requestLayout()
            invalidate()
        }

        override fun onTouchEvent(event: MotionEvent): Boolean =
            when (event.action == ACTION_DOWN && imageBorderRectViewDomain.contains(event)) {
                true -> {
                    edgeCandidateYsViewDomain.forEachIndexed { selectedEdgeCandidateIndex, y ->
                        if (event.onHorizontalLine(y, TOUCH_THRESHOLD)) {
                            with(viewModel.selectedEdgeCandidateIndices) {
                                when {
                                    value == null || value!!.second != null -> postValue(selectedEdgeCandidateIndex to null)
                                    value?.first == selectedEdgeCandidateIndex -> postValue(null)
                                    else -> postValue(value!!.first to selectedEdgeCandidateIndex)
                                }
                            }
                        }
                    }
                    true
                }

                false -> false
            }

        override fun onDraw(canvas: Canvas) {
            if (!isSetUp)
                return

            with(canvas) {
                drawLines(edgeCandidatePointsViewDomain, edgeCandidatePaint)

                viewModel.selectedEdgeCandidateIndices.value?.let {
                    drawSelectedEdgeHighlighting(it.first)

                    if (it.second != null) {
                        drawSelectedEdgeHighlighting(it.second!!)
                        drawCropMask()
                    }
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