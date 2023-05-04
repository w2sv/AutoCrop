package com.w2sv.autocrop.ui.views

import android.view.View
import com.w2sv.androidutils.ui.views.show

fun Iterable<View>.visualize(type: VisualizationType) {
    forEach {
        when (type) {
            VisualizationType.Instant -> it.show()
            VisualizationType.FadeIn -> it.fadeIn()
        }
    }
}

enum class VisualizationType {
    Instant,
    FadeIn
}