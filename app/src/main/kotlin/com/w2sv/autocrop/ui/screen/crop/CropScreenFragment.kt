package com.w2sv.autocrop.ui.screen.crop

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.w2sv.androidutils.widget.showToast
import com.w2sv.autocrop.AppFragment
import com.w2sv.autocrop.R
import com.w2sv.autocrop.databinding.CropBinding
import com.w2sv.autocrop.model.CropResults
import com.w2sv.autocrop.ui.screen.CropBundleViewModel
import com.w2sv.autocrop.ui.screen.cropNavGraphViewModel
import com.w2sv.autocrop.util.extensions.launchAfterShortDelay
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CropScreenFragment
    : AppFragment<CropBinding>(CropBinding::class.java) {

    private val viewModel by viewModels<CropScreenViewModel>()
    private val cropBundleVM by cropNavGraphViewModel<CropBundleViewModel>()

    override val onBackPressed: () -> Unit
        get() = {
            viewModel.backPressListener(
                onFirstPress = {
                    requireContext().showToast(getString(R.string.tap_again_to_cancel))
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
                onCropBundle = cropBundleVM::addCropBundle,
                onFinishedListener = ::invokeSubsequentScreen
            )
        }
    }

    private fun invokeSubsequentScreen(cropResults: CropResults) {
        if (cropBundleVM.cropBundles.isNotEmpty())
            navController.navigate(CropScreenFragmentDirections.navigateToCropPagerScreen(cropResults))
        else
            launchAfterShortDelay {  // to assure progress bar having reached 100% before UI change
                navController.navigate(CropScreenFragmentDirections.navigateToCroppingFailedScreen())
            }
    }
}