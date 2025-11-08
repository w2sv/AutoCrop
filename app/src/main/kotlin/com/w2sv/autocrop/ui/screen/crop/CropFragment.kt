package com.w2sv.autocrop.ui.screen.crop

import android.os.Bundle
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.w2sv.autocrop.ui.ComposeAppFragment
import com.w2sv.autocrop.ui.screen.cropSessionInjectedViewModel
import com.w2sv.autocrop.ui.util.navigateAnimatedAndPopCurrentDestination
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CropFragment : ComposeAppFragment() {

    private val viewModel by cropSessionInjectedViewModel<CropViewModel, CropViewModel.Factory>()

    @Composable
    override fun ScreenContent() {
        val state by viewModel.screenState.collectAsStateWithLifecycle()

        state.NavigateWhenCroppingFinished(
            delay = 1000,
            onAnySuccessfulCrops = {
                navController.navigateAnimatedAndPopCurrentDestination(
                    CropFragmentDirections.navigateToCropPagerScreen()
                )
            },
            onNoSuccessfulCrops = {
                navController.navigateAnimatedAndPopCurrentDestination(
                    CropFragmentDirections.navigateToCroppingFailedScreen()
                )
            }
        )

        CropScreen(state = state)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                viewModel.cropScreenshots(contentResolver = requireContext().contentResolver)
            }
        }
    }
}
