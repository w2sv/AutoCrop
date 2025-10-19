package com.w2sv.autocrop.ui.screen.cropadjustment

import android.content.Context
import android.graphics.Bitmap
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsIgnoringVisibility
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.graphics.createBitmap
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.w2sv.autocrop.R
import com.w2sv.autocrop.ui.ComposeAppFragment
import com.w2sv.autocrop.ui.screen.cropSessionInjectedViewModel
import com.w2sv.autocrop.ui.screen.cropadjustment.model.AdjustmentModeState
import com.w2sv.autocrop.ui.screen.cropadjustment.model.AdjustmentViewState
import com.w2sv.autocrop.ui.screen.cropadjustment.view.CropAdjustmentView
import com.w2sv.autocrop.ui.theme.AppTheme
import com.w2sv.autocrop.ui.util.hideNavigationBar
import com.w2sv.autocrop.ui.util.showSystemBars
import com.w2sv.domain.model.CropEdges
import dagger.hilt.android.AndroidEntryPoint
import slimber.log.i

@AndroidEntryPoint
class CropAdjustmentFragment : ComposeAppFragment() {

    private val viewModel by cropSessionInjectedViewModel<CropAdjustmentViewModel, CropAdjustmentViewModel.Factory>()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        hideNavigationBar()
    }

    override fun onDestroy() {
        super.onDestroy()
        showSystemBars()
    }

    @Composable
    override fun ScreenContent() {
        val viewState by viewModel.viewState.collectAsStateWithLifecycle()
        AdjustmentScreen(
            state = viewState,
            image = viewModel.screenshotBitmap,
            onModeStateChanged = viewModel::updateAdjustmentModeState,
            onReset = viewModel::resetState,
            onApply = {
                viewModel.applyAdjustedEdges()
                navController.popBackStack()
            },
            onBack = navController::popBackStack
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AdjustmentScreen(
    state: AdjustmentViewState,
    image: Bitmap,
    onModeStateChanged: (AdjustmentModeState) -> Unit,
    onReset: () -> Unit,
    onApply: () -> Unit,
    onBack: () -> Unit
) {
    Scaffold(contentWindowInsets = WindowInsets.statusBarsIgnoringVisibility) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            CropAdjustmentView(
                state = state,
                image = image,
                onModeStateChanged = onModeStateChanged
            )
            TopButtons(
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
    cropHasBeenAdjusted: Boolean,
    onReset: () -> Unit,
    onApply: () -> Unit,
    onBack: () -> Unit
) {
    val modifier = Modifier.padding(top = 24.dp)
    val filledIconButtonColors =
        IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.5f))

    FilledIconButton(
        onClick = onBack,
        modifier = modifier
            .padding(start = 8.dp)
            .size(48.dp),
        colors = filledIconButtonColors
    ) {
        Icon(Icons.AutoMirrored.Default.ArrowBack, contentDescription = null)
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
            onClick = onReset,
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
            onClick = onApply,
            enabled = cropHasBeenAdjusted,
            colors = filledIconButtonColors.copy(contentColor = Color.Green),
            modifier = Modifier.size(48.dp)
        ) {
            Icon(
                Icons.Default.Check,
                contentDescription = null
            )
        }
    }
}

@Composable
private fun CropAdjustmentView(
    state: AdjustmentViewState,
    image: Bitmap,
    onModeStateChanged: (AdjustmentModeState) -> Unit,
    modifier: Modifier = Modifier
) {
    AndroidView(
        modifier = modifier,
        factory = {
            i { "Running factory" }
            CropAdjustmentView(it).apply {
                initialize(
                    image = image,
                    cropEdges = state.adjustedEdges
                        ?: state.originalEdges
                )
                adjustmentModeStateChangedListener = onModeStateChanged
            }
        },
        update = { view ->
            state.adjustedEdges?.let { view.updateFromEdges(it) }
        }
    )
}

@Preview
@Composable
private fun Prev() {
    val originalEdges = CropEdges(200, 1200)

    AppTheme {
        AdjustmentScreen(
            AdjustmentViewState(
                originalEdges = originalEdges,
                modeState = AdjustmentModeState.Manual(CropEdges(400, 1200))
            ),
            createBitmap(800, 1600),
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
