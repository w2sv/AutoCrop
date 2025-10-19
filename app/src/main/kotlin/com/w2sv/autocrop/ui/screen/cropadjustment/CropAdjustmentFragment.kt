package com.w2sv.autocrop.ui.screen.cropadjustment

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.w2sv.autocrop.ui.ComposeAppFragment
import com.w2sv.autocrop.ui.screen.cropSessionInjectedViewModel
import com.w2sv.autocrop.ui.util.hideNavigationBar
import com.w2sv.autocrop.ui.util.showSystemBars
import dagger.hilt.android.AndroidEntryPoint

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
        CropAdjustmentScreen(
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
