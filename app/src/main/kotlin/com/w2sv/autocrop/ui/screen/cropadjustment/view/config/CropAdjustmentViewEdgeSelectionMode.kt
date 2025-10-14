// package com.w2sv.autocrop.ui.screen.cropadjustment.view.config
//
// import com.w2sv.autocrop.R
//
// class CropAdjustmentViewEdgeSelectionMode : CropAdjustmentViewMode {
//
//    private var drawCandidates: Boolean = false
//
//    private fun onEdgeCandidatesSelectionStateChanged(state: EdgeSelectionState) {
//        when (state) {
//            is EdgeSelectionState.SelectedBoth -> {
//                cropRectViewDomain.setVerticalEdges(
//                    edgeCandidateYsViewDomain[state.indexTopEdge],
//                    edgeCandidateYsViewDomain[state.indexBottomEdge]
//                )
//                viewModel.postCropEdges()
//            }
//
//            else -> viewModel.postCropEdges(null)
//        }
//
//        invalidate()
//    }
//
//    override fun setUp() {
//        viewModel.postEdgeSelectionState(EdgeSelectionState.Unselected)
//        resetCropRectViewDomain()
//
//        animateImageTo(defaultImageMatrix) {
//            resetImageBorderRectViewDomain()
//            drawCandidates = true
//            invalidate()
//
//            viewModel.edgeSelectionState.observe(findViewTreeLifecycleOwner()!!) {
//                onEdgeCandidatesSelectionStateChanged(it)
//            }
//        }
//    }
//
//    override fun onTouchEvent(event: MotionEvent): Boolean =
//        when (event.action == ACTION_DOWN && imageBorderRectViewDomain.contains(event, TOUCH_TOLERANCE_MARGIN)) {
//            true -> {
//                edgeCandidateYsViewDomain.forEachIndexed { selectedEdgeCandidateIndex, y ->
//                    if (event.isOnHorizontalLine(y, TOUCH_TOLERANCE_MARGIN)) {
//                        viewModel.postEdgeSelectionState(
//                            when (val state = viewModel.edgeSelectionState.value!!) {
//                                is EdgeSelectionState.Unselected, is EdgeSelectionState.SelectedBoth ->
//                                    EdgeSelectionState.SelectedFirst(selectedEdgeCandidateIndex)
//
//                                is EdgeSelectionState.SelectedFirst -> {
//                                    if (state.index == selectedEdgeCandidateIndex)
//                                        EdgeSelectionState.Unselected
//                                    else
//                                        listOf(state.index, selectedEdgeCandidateIndex).sorted().run {
//                                            EdgeSelectionState.SelectedBoth(get(0), get(1))
//                                        }
//                                }
//                            }
//                        )
//                        return true
//                    }
//                }
//                false
//            }
//
//            false -> false
//        }
//
//    override fun onDraw(canvas: Canvas) {
//        if (drawCandidates) {
//            canvas.drawEdgeCandidates()
//            canvas.drawEdgeIndicationTriangles()
//
//            if (viewModel.edgeSelectionState.value is EdgeSelectionState.SelectedBoth) {
//                canvas.drawCropMask()
//            }
//        }
//    }
//
//    private fun Canvas.drawEdgeCandidates() {
//        edgeCandidateLinesViewDomain.forEachIndexed { i, floats ->
//            drawLine(
//                floats[0],
//                floats[1],
//                floats[2],
//                floats[3],
//                if (viewModel.edgeSelectionState.value!!.isSelected(i))
//                    selectedEdgeCandidatePaint
//                else
//                    unselectedEdgeCandidatePaint
//            )
//        }
//    }
//
//    private fun Canvas.drawEdgeIndicationTriangles() {
//        edgeCandidateYsViewDomain.forEachIndexed { i, y ->
//            val paint = if (viewModel.edgeSelectionState.value!!.isSelected(i))
//                selectedTrianglePaint
//            else
//                unselectedTrianglePaint
//
//            drawPath(
//                pathTriangleWTipLeft(
//                    xEdgeIndicationTriangleWTipLeft,
//                    y
//                ),
//                paint
//            )
//
//            drawPath(
//                pathTriangleWTipRight(
//                    xEdgeIndicationTriangleWTipRight,
//                    y
//                ),
//                paint
//            )
//        }
//    }
//
//    private val xEdgeIndicationTriangleWTipLeft: Float by lazy {
//        imageBorderRectViewDomain.right + HORIZONTAL_OFFSET_EDGE_INDICATION_TRIANGLE
//    }
//    private val xEdgeIndicationTriangleWTipRight: Float by lazy {
//        imageBorderRectViewDomain.left - HORIZONTAL_OFFSET_EDGE_INDICATION_TRIANGLE
//    }
//
//    private val unselectedTrianglePaint = Paint()
//        .apply {
//            style = Paint.Style.FILL
//            isAntiAlias = true
//            color = context.getColor(UNSELECTED_EDGE_CANDIDATE_COLOR)
//        }
//
//    private val selectedTrianglePaint = Paint()
//        .apply {
//            style = Paint.Style.FILL
//            isAntiAlias = true
//            color = context.getColor(SELECTED_EDGE_CANDIDATE_COLOR)
//        }
//
//    private fun pathTriangleWTipLeft(x: Float, y: Float): Path =
//        Path()
//            .apply {
//                moveTo(x, y) // Tip
//                lineTo(
//                    x + EDGE_INDICATION_TRIANGLE_EDGE_LENGTH,
//                    y + EDGE_INDICATION_TRIANGLE_EDGE_LENGTH_HALVE
//                ) // Right bottom
//                lineTo(
//                    x + EDGE_INDICATION_TRIANGLE_EDGE_LENGTH,
//                    y - EDGE_INDICATION_TRIANGLE_EDGE_LENGTH_HALVE
//                ) // Right top
//                lineTo(x, y) // Back to tip
//                close()
//            }
//
//    private fun pathTriangleWTipRight(x: Float, y: Float): Path =
//        Path()
//            .apply {
//                moveTo(x, y) // Tip
//                lineTo(
//                    x - EDGE_INDICATION_TRIANGLE_EDGE_LENGTH,
//                    y + EDGE_INDICATION_TRIANGLE_EDGE_LENGTH_HALVE
//                ) // Left bottom
//                lineTo(
//                    x - EDGE_INDICATION_TRIANGLE_EDGE_LENGTH,
//                    y - EDGE_INDICATION_TRIANGLE_EDGE_LENGTH_HALVE
//                ) // Left top
//                lineTo(x, y) // Back to tip
//                close()
//            }
//
//    private val selectedEdgeCandidatePaint = Paint()
//        .apply {
//            color = context.getColor(SELECTED_EDGE_CANDIDATE_COLOR)
//            strokeWidth = 5f
//            style = Paint.Style.STROKE
//        }
//
//    private val unselectedEdgeCandidatePaint = Paint()
//        .apply {
//            color = context.getColor(UNSELECTED_EDGE_CANDIDATE_COLOR)
//            strokeWidth = 5f
//            style = Paint.Style.STROKE
//            pathEffect = DashPathEffect(floatArrayOf(20f, 10f), 0f)
//        }
//
//    private val edgeCandidateLinesViewDomain: List<Line> by lazy {
//        viewModel.edgeCandidateLinesViewDomainCache.get(imageMatrix)
//    }
//
//    private val edgeCandidateYsViewDomain: List<Float> by lazy {
//        viewModel.edgeCandidateYsViewDomainCache.get(imageMatrix)
//    }
//
//    companion object {
//        private val UNSELECTED_EDGE_CANDIDATE_COLOR = com.w2sv.core.common.R.color.light_gray
//        private val SELECTED_EDGE_CANDIDATE_COLOR = R.color.highlight
//        private const val EDGE_INDICATION_TRIANGLE_EDGE_LENGTH = 34f
//        private const val EDGE_INDICATION_TRIANGLE_EDGE_LENGTH_HALVE = 17f
//        private const val HORIZONTAL_OFFSET_EDGE_INDICATION_TRIANGLE = 12
//    }
// }
