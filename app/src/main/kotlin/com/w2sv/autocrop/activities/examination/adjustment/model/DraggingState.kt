package com.w2sv.autocrop.activities.examination.adjustment.model

sealed class DraggingState {
    class DraggingEdge(val edge: Edge) : DraggingState()
    object DraggingCropRect : DraggingState()
    object Idle : DraggingState()
}