package com.w2sv.autocrop.ui.screen.crop

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.delay

@Immutable
data class CropScreenState(val croppedCount: Int, val totalImageCount: Int, private val anythingSuccessfullyCropped: Boolean) {
    private val croppingFinished get() = croppedCount == totalImageCount

    val centerContent
        get() = when {
            !croppingFinished -> CenterContent.ProgressIndicator
            anythingSuccessfullyCropped -> CenterContent.SuccessIcon
            else -> CenterContent.ErrorIcon
        }

    @Composable
    fun NavigateWhenCroppingFinished(
        delay: Long,
        onAnySuccessfulCrops: () -> Unit,
        onNoSuccessfulCrops: () -> Unit
    ) {
        LaunchedEffect(croppingFinished) {
            if (!croppingFinished) return@LaunchedEffect

            delay(delay)
            if (anythingSuccessfullyCropped) {
                onAnySuccessfulCrops()
            } else {
                onNoSuccessfulCrops()
            }
        }
    }

    enum class CenterContent {
        ProgressIndicator,
        SuccessIcon,
        ErrorIcon
    }
}
