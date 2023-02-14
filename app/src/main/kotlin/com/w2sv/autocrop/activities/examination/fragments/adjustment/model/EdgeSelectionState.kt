package com.w2sv.autocrop.activities.examination.fragments.adjustment.model

sealed class EdgeSelectionState(val indices: Set<Int>) {
    object Unselected : EdgeSelectionState(setOf())
    class SelectedFirst(val index: Int) : EdgeSelectionState(setOf(index))
    class SelectedBoth(val indexTopEdge: Int, val indexBottomEdge: Int) : EdgeSelectionState(
        setOf(
            indexTopEdge,
            indexBottomEdge
        )
    )
}
