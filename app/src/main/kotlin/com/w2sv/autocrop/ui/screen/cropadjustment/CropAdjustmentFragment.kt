package com.w2sv.autocrop.ui.screen.cropadjustment

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.w2sv.autocrop.ui.ComposeAppFragment
import com.w2sv.autocrop.ui.screen.cropSessionInjectedViewModel
import com.w2sv.autocrop.ui.util.hideNavigationBar
import com.w2sv.autocrop.ui.util.postponeEnterTransition
import com.w2sv.autocrop.ui.util.showSystemBars
import com.w2sv.autocrop.ui.util.view.SharedElementTransitionState
import com.w2sv.autocrop.ui.util.view.inflateSharedElementTransition
import com.w2sv.autocrop.ui.util.view.onEnd
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CropAdjustmentFragment : ComposeAppFragment() {

    private val viewModel by cropSessionInjectedViewModel<CropAdjustmentViewModel, CropAdjustmentViewModel.Factory>()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        hideNavigationBar()
        sharedElementEnterTransition = inflateSharedElementTransition(context)?.onEnd {
            viewModel.setSharedElementTransitionState(SharedElementTransitionState.Idle)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        postponeEnterTransition(view)
    }

    override fun onDestroy() {
        super.onDestroy()
        showSystemBars()
    }

    @Composable
    override fun ScreenContent() {
        val viewState by viewModel.viewState.collectAsStateWithLifecycle()
        val sharedElementTransitionState by viewModel.sharedElementTransitionState.collectAsStateWithLifecycle()
        val onBack: () -> Unit = remember {
            {
                viewModel.setSharedElementTransitionState(SharedElementTransitionState.Exiting)
                navController.popBackStack()
            }
        }
        CropAdjustmentScreen(
            state = viewState,
            sharedElementTransitionState = sharedElementTransitionState,
            onModeStateChanged = viewModel::updateAdjustmentModeState,
            onReset = viewModel::resetState,
            onApply = {
                viewModel.applyAdjustedEdges()
                onBack()
            },
            onBack = onBack
        )
    }
}
