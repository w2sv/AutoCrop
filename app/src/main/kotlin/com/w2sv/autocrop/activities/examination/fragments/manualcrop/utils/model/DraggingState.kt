package com.w2sv.autocrop.activities.examination.fragments.manualcrop.utils.model

sealed class DraggingState {
    data class DraggingCorner(var corner: Corner) : DraggingState()
    data class DraggingEdge(var edge: Edge) : DraggingState()

    object DraggingCropRect : DraggingState()
    object Idle : DraggingState()
}