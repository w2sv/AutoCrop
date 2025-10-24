package com.w2sv.autocrop.ui.screen.cropadjustment.model

import android.graphics.Bitmap
import com.w2sv.domain.model.CropEdges

data class AdjustmentViewState(
    val imageBitmap: Bitmap,
    val cropBitmap: Bitmap,
    val originalEdges: CropEdges,
    val sharedElementTransitionName: String,
    val modeState: AdjustmentModeState,
    val adjustmentHasBeenApplied: Boolean = false
) {
    val adjustedEdges by modeState::adjustedEdges
    val appliedEdges get() = if (adjustmentHasBeenApplied) checkNotNull(adjustedEdges) else originalEdges

    val drawEdges
        get() = adjustedEdges
            ?: originalEdges

    val adjustmentCanBeApplied: Boolean
        get() = adjustedEdges !in listOf(null, originalEdges)
}
