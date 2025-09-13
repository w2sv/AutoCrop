package com.w2sv.autocrop.ui.util.compose

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.EnterExitState
import androidx.compose.runtime.Composable
import com.w2sv.composed.OnDispose

@Composable
fun AnimatedVisibilityScope.OnExitAnimationFinished(callback: () -> Unit) {
    OnDispose { if (transition.targetState == EnterExitState.PostExit) callback() }
}
