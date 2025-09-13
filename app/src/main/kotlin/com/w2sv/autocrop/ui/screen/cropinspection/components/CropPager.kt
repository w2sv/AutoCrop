package com.w2sv.autocrop.ui.screen.cropinspection.components

import android.graphics.Bitmap
import android.widget.ImageView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.w2sv.autocrop.ui.util.compose.OnExitAnimationFinished
import com.w2sv.composed.OnDispose
import com.w2sv.domain.model.Crop

val Crop.transitionName: String get() = hashCode().toString()

private const val exitAnimationDuration = 500

@Composable
fun CropPager(
    state: PagerState,
    getCrop: (Int) -> Crop,
    exitAnimationPageIndex: Int?,
    onExitAnimationFinished: () -> Unit,
    onImageViewReady: (String, ImageView) -> Unit,
    onImageViewDisposed: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    HorizontalPager(
        state = state,
        modifier = modifier,
        key = { getCrop(it).hashCode() }
    ) { pageIndex ->
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            AnimatedVisibility(
                visible = exitAnimationPageIndex != pageIndex,
                enter = EnterTransition.None,
                exit = shrinkOut(animationSpec = tween(durationMillis = exitAnimationDuration), shrinkTowards = Alignment.Center) + fadeOut(
                    animationSpec = tween(durationMillis = exitAnimationDuration)
                )
            ) {
                OnExitAnimationFinished(onExitAnimationFinished)
                val crop = getCrop(pageIndex)
                SharedElementImage(
                    bitmap = crop.bitmap,
                    transitionName = crop.transitionName,
                    onImageViewReady = onImageViewReady,
                    onImageViewDisposed = onImageViewDisposed
                )
            }
        }
    }
}

@Composable
private fun SharedElementImage(
    bitmap: Bitmap,
    transitionName: String,
    onImageViewReady: (String, ImageView) -> Unit,
    onImageViewDisposed: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OnDispose { onImageViewDisposed(transitionName) }
    AndroidView(
        factory = { context ->
            ImageView(context).apply {
                setImageBitmap(bitmap)
                scaleType = ImageView.ScaleType.FIT_CENTER
                this.transitionName = transitionName
                onImageViewReady(transitionName, this)
            }
        },
        modifier = modifier
    )
}
