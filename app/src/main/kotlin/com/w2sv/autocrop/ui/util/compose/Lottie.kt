package com.w2sv.autocrop.ui.util.compose

import androidx.annotation.RawRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.rememberLottieComposition

@Composable
fun LottieButton(
    @RawRes animationRes: Int,
    animationProgress: () -> Float,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(animationRes))

    LottieAnimation(
        modifier = modifier
            .clip(CircleShape)
            .clickable(onClick = onClick),
        composition = composition,
        progress = animationProgress
    )
}
