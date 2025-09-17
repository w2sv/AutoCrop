package com.w2sv.autocrop.ui.screen.cropadjustment.model

sealed class EdgeSelectionState(private val indices: Set<Int>) {

    fun isSelected(index: Int): Boolean =
        indices.contains(index)

    data object Unselected : EdgeSelectionState(setOf())
    data class SelectedFirst(val index: Int) : EdgeSelectionState(setOf(index))
    data class SelectedBoth(val indexTopEdge: Int, val indexBottomEdge: Int) : EdgeSelectionState(setOf(indexTopEdge, indexBottomEdge))
}
