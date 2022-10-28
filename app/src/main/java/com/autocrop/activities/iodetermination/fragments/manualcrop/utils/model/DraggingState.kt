package com.autocrop.activities.iodetermination.fragments.manualcrop.utils.model

sealed class DraggingState {
    data class DraggingCorner(var corner: Corner) : DraggingState()
    data class DraggingEdge(var edge: Edge) : DraggingState()

    object DraggingCropRect : DraggingState()
    object Idle : DraggingState()
}