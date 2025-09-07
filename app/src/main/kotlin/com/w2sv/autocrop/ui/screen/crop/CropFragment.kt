package com.w2sv.autocrop.ui.screen.crop

import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import com.w2sv.androidutils.BackPressHandler
import com.w2sv.androidutils.widget.showToast
import com.w2sv.autocrop.databinding.CropBinding
import com.w2sv.autocrop.ui.AppFragment
import com.w2sv.autocrop.ui.designsystem.navigateAnimatedAndPopCurrentDestination
import com.w2sv.autocrop.ui.screen.cropSessionInjectedViewModel
import com.w2sv.autocrop.ui.util.Constant
import com.w2sv.core.common.R.string as Strings
import com.w2sv.kotlinutils.threadUnsafeLazy
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CropFragment : AppFragment<CropBinding>(CropBinding::class.java) {

    private val viewModel by cropSessionInjectedViewModel<CropViewModel, CropViewModel.Factory>()

    private val backPressListener by threadUnsafeLazy {
        BackPressHandler(
            coroutineScope = lifecycleScope,
            confirmationWindowDuration = Constant.BACKPRESS_CONFIRMATION_WINDOW_DURATION
        )
    }

    override val onBackPressed: () -> Unit
        get() = {
            backPressListener(
                onFirstPress = {
                    requireContext().showToast(getString(Strings.tap_again_to_cancel))
                },
                onSecondPress = {
                    navController.popBackStack()
                }
            )
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
            croppingProgressBar.max = viewModel.screenshotCount

            viewModel.cropProgress.observe(viewLifecycleOwner) {
                progressTv.updateText(it, viewModel.screenshotCount)
                croppingProgressBar.progress = it
            }
        }
    }

    override fun onResume() {
        super.onResume()

        lifecycleScope.launch {
            viewModel.cropScreenshots(
                contentResolver = requireContext().contentResolver,
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
        }
    }
}
