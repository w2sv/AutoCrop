package com.w2sv.autocrop.ui.screen.cropadjustment

import android.graphics.Bitmap
import android.graphics.Matrix
import android.widget.ImageView
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsIgnoringVisibility
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.graphics.createBitmap
import com.w2sv.autocrop.R
import com.w2sv.autocrop.ui.screen.cropadjustment.model.AdjustmentModeState
import com.w2sv.autocrop.ui.screen.cropadjustment.model.AdjustmentViewState
import com.w2sv.autocrop.ui.screen.cropadjustment.view.CropAdjustmentView
import com.w2sv.autocrop.ui.theme.AppTheme
import com.w2sv.autocrop.ui.util.compose.debounceClick
import com.w2sv.autocrop.ui.util.view.SharedElementTransitionState
import com.w2sv.autocrop.ui.util.view.getScaleY
import com.w2sv.domain.model.CropEdges

@Composable
fun CropAdjustmentScreen(
    state: AdjustmentViewState,
    sharedElementTransitionState: SharedElementTransitionState,
    onModeStateChanged: (AdjustmentModeState) -> Unit,
    onReset: () -> Unit,
    onApply: () -> Unit,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)
    Scaffold(contentWindowInsets = WindowInsets.statusBarsIgnoringVisibility) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            CropAdjustmentView(
                state = state,
                sharedElementTransitionState = sharedElementTransitionState,
                onModeStateChanged = onModeStateChanged,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 22.dp)
            )
            TopButtons(
                backButtonEnabled = sharedElementTransitionState.isIdle,
                cropHasBeenAdjusted = state.adjustmentCanBeApplied,
                onReset = onReset,
                onApply = onApply,
                onBack = onBack
            )
        }
    }
}

@Composable
private fun BoxScope.TopButtons(
    backButtonEnabled: Boolean,
    cropHasBeenAdjusted: Boolean,
    onReset: () -> Unit,
    onApply: () -> Unit,
    onBack: () -> Unit
) {
    val modifier = Modifier.padding(top = 24.dp)
    val filledIconButtonColors =
        IconButtonDefaults.filledIconButtonColors(containerColor = colorScheme.surfaceContainerHighest.copy(alpha = 0.5f))

    FilledIconButton(
        onClick = debounceClick(onClick = onBack),
        modifier = modifier
            .padding(start = 8.dp)
            .size(48.dp),
        colors = filledIconButtonColors,
        enabled = backButtonEnabled
    ) {
        Icon(painterResource(R.drawable.ic_arrow_back_24), contentDescription = null)
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .align(Alignment.TopEnd)
            .then(modifier)
            .padding(end = 12.dp)
    ) {
        FilledIconButton(
            onClick = debounceClick(onClick = onReset),
            enabled = cropHasBeenAdjusted,
            colors = filledIconButtonColors,
            modifier = Modifier.size(48.dp)
        ) {
            Icon(
                painterResource(R.drawable.ic_restart_24),
                contentDescription = null
            )
        }
        FilledIconButton(
            onClick = debounceClick(onClick = onApply),
            enabled = cropHasBeenAdjusted,
            colors = filledIconButtonColors.copy(contentColor = Color.Green),
            modifier = Modifier.size(48.dp)
        ) {
            Icon(
                painterResource(R.drawable.ic_check_24),
                contentDescription = null
            )
        }
    }
}

@Composable
private fun CropAdjustmentView(
    state: AdjustmentViewState,
    sharedElementTransitionState: SharedElementTransitionState,
    onModeStateChanged: (AdjustmentModeState) -> Unit,
    modifier: Modifier = Modifier
) {
    var imageMatrix by remember { mutableStateOf(Matrix()) }

    Box(modifier = modifier, contentAlignment = Alignment.TopCenter) {
        AndroidView(
            factory = { context ->
                CropAdjustmentView(context)
                    .apply {
                        initialize(
                            image = state.imageBitmap,
                            cropEdges = state.drawEdges
                        )
                        adjustmentModeStateChangedListener = onModeStateChanged
                        imageMatrixChangedListener = { imageMatrix = it }
                    }
            },
            update = { view ->
                state.adjustedEdges?.let { view.updateFromEdges(it) }
                view.overlays = when (sharedElementTransitionState) {
                    SharedElementTransitionState.Entering -> CropAdjustmentView.Overlays.HideCropMaskAndDrawCropAreaBlack
                    else -> CropAdjustmentView.Overlays.CropMask
                }
            }
        )
        if (!sharedElementTransitionState.isIdle) {
            CropOverlay(
                cropBitmap = state.cropBitmap,
                imageMatrix = imageMatrix,
                transitionName = state.sharedElementTransitionName,
                modifier = Modifier.graphicsLayer { translationY = state.appliedEdges.top.toFloat() * imageMatrix.getScaleY() }
            )
        }
    }
}

@Composable
private fun CropOverlay(
    cropBitmap: Bitmap,
    imageMatrix: Matrix,
    transitionName: String,
    modifier: Modifier = Modifier
) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            ImageView(context).apply {
                this.transitionName = transitionName
                setImageBitmap(cropBitmap)
                scaleType = ImageView.ScaleType.MATRIX
                this.imageMatrix = imageMatrix
            }
        },
        update = { view -> view.imageMatrix = imageMatrix }
    )
}

@Preview
@Composable
private fun Prev() {
    val imageBitmap = createBitmap(800, 1600)
    val cropBitmap = createBitmap(800, 400)
    val originalEdges = CropEdges(200, 1200)
    val adjustedEdges = CropEdges(400, 1200)

    AppTheme {
        CropAdjustmentScreen(
            AdjustmentViewState(
                imageBitmap = imageBitmap,
                cropBitmap = cropBitmap,
                originalEdges = originalEdges,
                sharedElementTransitionName = "",
                modeState = AdjustmentModeState.Manual(adjustedEdges)
            ),
            SharedElementTransitionState.Idle,
            {},
            {},
            {},
            {}
        )
    }
}

// @Composable
// private fun ModeSelectionButtons(
//    selectedMode: CropAdjustmentMode,
//    onModeSelected: (CropAdjustmentMode) -> Unit,
//    modifier: Modifier = Modifier
// ) {
//    SingleChoiceSegmentedButtonRow(modifier) {
//        CropAdjustmentMode.entries.forEachIndexed { i, mode ->
//            val isSelected = remember(selectedMode) { mode == selectedMode }
//            SegmentedButton(
//                selected = isSelected,
//                onClick = {
//                    if (!isSelected) {
//                        onModeSelected(mode)
//                    }
//                },
//                shape = SegmentedButtonDefaults.itemShape(
//                    index = i,
//                    count = 2
//                )
//            ) {
//                Text(text = stringResource(id = mode.labelRes))
//            }
//        }
//    }
// }
