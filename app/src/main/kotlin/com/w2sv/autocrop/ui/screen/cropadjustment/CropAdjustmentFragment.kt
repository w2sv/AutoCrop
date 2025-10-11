package com.w2sv.autocrop.ui.screen.cropadjustment

import android.graphics.Bitmap
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
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
import com.w2sv.domain.model.CropAdjustmentMode
import com.w2sv.domain.model.CropEdges
import dagger.hilt.android.AndroidEntryPoint
import slimber.log.i

//@AndroidEntryPoint
//class CropAdjustmentFragment : ViewBoundAppFragment<CropAdjustmentBinding>(CropAdjustmentBinding::class.java) {
//
//    private val viewModel by cropSessionInjectedViewModel<CropAdjustmentViewModel, CropAdjustmentViewModel.Factory>()
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        val viewState = viewModel.viewState.value
//        binding.cropAdjustmentView.apply {
//            initialize(viewModel.screenshotBitmap, viewState.originalEdges)
//            setModeConfig(viewState.modeState.mode)
//            setAdjustmentModeStateChangedListener {}
//        }
//    }
//}

@AndroidEntryPoint
class CropAdjustmentFragment : ComposeAppFragment() {

    private val viewModel by cropSessionInjectedViewModel<CropAdjustmentViewModel, CropAdjustmentViewModel.Factory>()

    @Composable
    override fun ScreenContent() {
        val viewState by viewModel.viewState.collectAsStateWithLifecycle()
        CropAdjustmentScreen(
            viewState = viewState,
            image = viewModel.screenshotBitmap,
            onModeSelected = viewModel::updateAdjustmentMode,
            onModeStateChanged = viewModel::updateAdjustmentModeState
        )
    }
}

@Composable
private fun CropAdjustmentScreen(
    viewState: AdjustmentViewState,
    image: Bitmap,
    onModeSelected: (CropAdjustmentMode) -> Unit,
    onModeStateChanged: (AdjustmentModeState) -> Unit
) {
    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(top = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ModeSelectionButtons(
                selectedMode = viewState.modeState.mode,
                onModeSelected = onModeSelected,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp)
            )

            AndroidView(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                factory = {
                    i { "Running factory" }
                    CropAdjustmentView(it).apply {
                        initialize(image = image, cropEdges = viewState.originalEdges)
                        setModeConfig(viewState.modeState.mode)
                        setAdjustmentModeStateChangedListener(onModeStateChanged)
                    }
                },
                update = { view ->
                    i { "Running onUpdate" }
                }
            )
        }
    }
}

@Preview
@Composable
private fun Prev() {
    val originalEdges = CropEdges(200, 1200)

    AppTheme {
        CropAdjustmentScreen(
            AdjustmentViewState(
                originalEdges = originalEdges,
                modeState = AdjustmentModeState.Manual(originalEdges)
            ),
            createBitmap(800, 1600),
            {},
            {}
        )
    }
}

@Composable
private fun ModeSelectionButtons(
    selectedMode: CropAdjustmentMode,
    onModeSelected: (CropAdjustmentMode) -> Unit,
    modifier: Modifier = Modifier
) {
    SingleChoiceSegmentedButtonRow(modifier) {
        CropAdjustmentMode.entries.forEachIndexed { i, mode ->
            val isSelected = remember(selectedMode) { mode == selectedMode }
            SegmentedButton(
                selected = isSelected,
                onClick = {
                    if (!isSelected) {
                        onModeSelected(mode)
                    }
                },
                shape = SegmentedButtonDefaults.itemShape(
                    index = i,
                    count = 2
                )
            ) {
                Text(text = stringResource(id = mode.labelRes))
            }
        }
    }
}
