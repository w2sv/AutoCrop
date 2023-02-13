package com.w2sv.autocrop.activities.examination.fragments.adjustment.model

sealed class DraggingState {
    class DraggingEdge(var edge: Edge) : DraggingState()
    object DraggingCropRect : DraggingState()
    object Idle : DraggingState()
}