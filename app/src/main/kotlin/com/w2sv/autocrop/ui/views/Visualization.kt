package com.w2sv.autocrop.ui.views

import android.view.View
import com.w2sv.androidutils.ui.views.show

fun Iterable<View>.visualize(method: VisualizationMethod) {
    forEach {
        method.invoke(it)
    }
}

enum class VisualizationMethod(val invoke: (View) -> Unit) {
    Instantaneous({ it.show() }),
    FadeIn({ it.fadeIn() })
}