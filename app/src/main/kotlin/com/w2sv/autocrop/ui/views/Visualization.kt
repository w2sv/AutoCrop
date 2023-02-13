package com.w2sv.autocrop.ui.views

import android.view.View
import com.w2sv.onboarding.extensions.show

fun List<View?>.visualize(type: VisualizationType) {
    forEach {
        it?.let {
            when (type) {
                VisualizationType.Instant -> it.show()
                VisualizationType.FadeIn -> it.fadeIn()
            }
        }
    }
}

enum class VisualizationType {
    Instant,
    FadeIn
}