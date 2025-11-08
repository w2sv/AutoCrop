package com.w2sv.autocrop.ui.screen.crop

import android.view.animation.OvershootInterpolator
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.w2sv.autocrop.ui.designsystem.compose.FlowFieldOverlayingScaffold
import com.w2sv.autocrop.ui.theme.AppTheme
import com.w2sv.autocrop.ui.util.compose.contentTransform
import com.w2sv.core.common.R

@Composable
fun CropScreen(state: CropScreenState) {
    FlowFieldOverlayingScaffold { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
            horizontalAlignment = Alignment.Companion.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            Text(
                stringResource(R.string.cropping_dot_dot_dot),
                style = MaterialTheme.typography.headlineMediumEmphasized
            )
            CenterContent(content = state.centerContent, modifier = Modifier.size(102.dp))
            Text(text = remember(state) { "${state.croppedCount}/${state.totalImageCount}" })
        }
    }
}

private val resultIconSize = 72.dp

@Composable
private fun CenterContent(content: CropScreenState.CenterContent, modifier: Modifier = Modifier) {
    AnimatedContent(
        targetState = content,
        contentAlignment = Alignment.Center,
        transitionSpec = { overshootFadeScaleTransform() },
        modifier = modifier
    ) {
        when (it) {
            CropScreenState.CenterContent.ProgressIndicator -> CircularWavyProgressIndicator(modifier = Modifier.size(92.dp))

            CropScreenState.CenterContent.SuccessIcon -> Icon(
                painterResource(com.w2sv.autocrop.R.drawable.ic_check_24),
                contentDescription = null,
                tint = Color.Green,
                modifier = Modifier.size(resultIconSize)
            )

            CropScreenState.CenterContent.ErrorIcon -> Icon(
                painterResource(R.drawable.ic_cancel_24),
                contentDescription = null,
                tint = colorScheme.error,
                modifier = Modifier.size(resultIconSize)
            )
        }
    }
}

private fun overshootFadeScaleTransform(): ContentTransform =
    contentTransform(
        fadeIn(animationSpec = tween(500)) + scaleIn(
            animationSpec = tween(
                durationMillis = 800,
                easing = Easing { fraction -> OvershootInterpolator(3f).getInterpolation(fraction) }
            )
        ),
        fadeOut(animationSpec = tween(300)) + scaleOut(animationSpec = tween(300))
    )

@Preview
@Composable
private fun Prev() {
    AppTheme {
        CropScreen(state = CropScreenState(5, 7, true))
    }
}
