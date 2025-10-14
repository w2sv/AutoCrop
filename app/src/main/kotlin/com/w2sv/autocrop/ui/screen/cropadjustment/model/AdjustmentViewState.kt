package com.w2sv.autocrop.ui.screen.cropadjustment.model

import com.w2sv.domain.model.CropEdges

data class AdjustmentViewState(val originalEdges: CropEdges, val modeState: AdjustmentModeState) {

    val adjustedEdges by modeState::adjustedEdges

    val adjustmentCanBeApplied: Boolean
        get() = adjustedEdges !in listOf(null, originalEdges)
}
