package com.w2sv.autocrop.ui.screen.cropadjustment.model

import com.w2sv.domain.model.CropAdjustmentMode
import com.w2sv.domain.model.CropEdges

sealed interface AdjustmentModeState {
    val adjustedEdges: CropEdges?

    val mode: CropAdjustmentMode
        get() = when (this) {
            is Manual -> CropAdjustmentMode.Manual
            is EdgeSelection -> CropAdjustmentMode.EdgeSelection
        }

    @JvmInline
    value class Manual(override val adjustedEdges: CropEdges) : AdjustmentModeState

    data class EdgeSelection(val state: State = State.Unselected) : AdjustmentModeState {
        override val adjustedEdges by state::cropEdges

        sealed class State(private val indices: Set<Int>) {

            fun isSelected(index: Int): Boolean =
                indices.contains(index)

            val cropEdges: CropEdges?
                get() = if (this is SelectedBoth) {
                    CropEdges(indexTopEdge, indexBottomEdge)
                }
                else {
                    null
                }

            data object Unselected : State(emptySet())
            data class SelectedFirst(val index: Int) : State(setOf(index))
            data class SelectedBoth(val indexTopEdge: Int, val indexBottomEdge: Int) : State(setOf(indexTopEdge, indexBottomEdge))
        }
    }
}
