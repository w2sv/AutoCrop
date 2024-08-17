package com.w2sv.autocrop.activities.examination.adjustment.model

sealed interface DraggingState {
    class DraggingEdge(val edge: Edge) : DraggingState
    data object DraggingCropRect : DraggingState
    data object Idle : DraggingState
}