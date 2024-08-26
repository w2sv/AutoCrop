package com.w2sv.autocrop.ui.util

import android.view.View

fun View.asSharedElement(transitionName: String): Pair<View, String> =
    apply { this.transitionName = transitionName } to transitionName