package com.w2sv.autocrop.ui.screen.cropadjustment

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.fragment.navArgs
import com.w2sv.autocrop.ui.ComposeAppFragment
import com.w2sv.autocrop.ui.screen.cropSessionInjectedViewModel
import com.w2sv.autocrop.ui.util.hideNavigationBar
import com.w2sv.autocrop.ui.util.postponeEnterTransition
import com.w2sv.autocrop.ui.util.showSystemBars
import com.w2sv.autocrop.ui.util.view.inflateTransition
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CropAdjustmentFragment : ComposeAppFragment() {

    private val viewModel by cropSessionInjectedViewModel<CropAdjustmentViewModel, CropAdjustmentViewModel.Factory>()
    private val navArgs by navArgs<CropAdjustmentFragmentArgs>()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        hideNavigationBar()
        sharedElementEnterTransition = inflateTransition(context, android.R.transition.move)
            ?.setDuration(1_000)
            ?.setInterpolator(AccelerateDecelerateInterpolator())
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
        CropAdjustmentScreen(
            state = viewState,
            image = viewModel.screenshotBitmap,
            sharedElementTransitionName = navArgs.transitionName,
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
