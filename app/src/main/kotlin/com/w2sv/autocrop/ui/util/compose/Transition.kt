package com.w2sv.autocrop.ui.util.compose

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.togetherWith

fun contentTransform(enter: EnterTransition, exit: ExitTransition): ContentTransform =
    enter togetherWith exit
