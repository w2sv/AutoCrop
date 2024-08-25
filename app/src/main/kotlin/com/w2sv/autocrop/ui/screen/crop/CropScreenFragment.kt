package com.w2sv.autocrop.ui.screen.crop

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.w2sv.androidutils.widget.showToast
import com.w2sv.autocrop.AppFragment
import com.w2sv.autocrop.R
import com.w2sv.autocrop.databinding.CropBinding
import com.w2sv.autocrop.util.extensions.launchAfterShortDelay
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CropScreenFragment
    : AppFragment<CropBinding>(CropBinding::class.java) {

    private val viewModel by viewModels<CropScreenViewModel>()

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
            croppingProgressBar.max = viewModel.nScreenshots
        }
        with(viewModel) {
            liveProgress.observe(viewLifecycleOwner) {
                binding.progressTv.updateText(it, nScreenshots)
                binding.croppingProgressBar.progress = it
            }
        }
    }

    override fun onResume() {
        super.onResume()

        lifecycleScope.launch {
            viewModel.cropCoroutine(requireContext().contentResolver) {
                invokeSubsequentScreen()
            }
        }
    }

    private fun invokeSubsequentScreen() {
        if (viewModel.cropBundles.isNotEmpty())
            navController.navigate(CropScreenFragmentDirections.navigateToCropPagerScreen())
        else
            launchAfterShortDelay {  // to assure progress bar having reached 100% before UI change
                navController.navigate(CropScreenFragmentDirections.navigateToCroppingFailedScreen())
            }
    }
}