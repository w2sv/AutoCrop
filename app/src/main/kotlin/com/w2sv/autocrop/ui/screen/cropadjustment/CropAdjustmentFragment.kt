package com.w2sv.autocrop.ui.screen.cropadjustment

import android.graphics.Bitmap
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.graphics.createBitmap
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.w2sv.autocrop.ui.ComposeAppFragment
import com.w2sv.autocrop.ui.screen.cropSessionInjectedViewModel
import com.w2sv.autocrop.ui.screen.cropadjustment.model.AdjustmentModeState
import com.w2sv.autocrop.ui.screen.cropadjustment.model.AdjustmentViewState
import com.w2sv.autocrop.ui.screen.cropadjustment.view.CropAdjustmentView
import com.w2sv.autocrop.ui.theme.AppTheme
import com.w2sv.domain.model.CropEdges
import dagger.hilt.android.AndroidEntryPoint
import slimber.log.i

@AndroidEntryPoint
class CropAdjustmentFragment : ComposeAppFragment() {

    private val viewModel by cropSessionInjectedViewModel<CropAdjustmentViewModel, CropAdjustmentViewModel.Factory>()

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
    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Default.ArrowBack, contentDescription = null)
                    }
                },
                actions = {
                    FilledIconButton(onClick = onReset, enabled = state.adjustmentCanBeApplied) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = null
                        )
                    }
                    FilledIconButton(onClick = onApply, enabled = state.adjustmentCanBeApplied) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        AndroidView(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .padding(top = 16.dp),
            factory = {
                i { "Running factory" }
                CropAdjustmentView(it).apply {
                    initialize(image = image, cropEdges = state.originalEdges)
                    setAdjustmentModeStateChangedListener(onModeStateChanged)
                }
            },
            update = { view ->
                i { "Running onUpdate" }
            }
        )
    }
}

@Preview
@Composable
private fun Prev() {
    val originalEdges = CropEdges(200, 1200)

    AppTheme {
        AdjustmentScreen(
            AdjustmentViewState(
                originalEdges = originalEdges,
                modeState = AdjustmentModeState.Manual(originalEdges)
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
