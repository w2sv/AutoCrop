package com.w2sv.autocrop.ui.screen.saveall

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.w2sv.androidutils.widget.showToast
import com.w2sv.autocrop.databinding.SaveAllBinding
import com.w2sv.autocrop.ui.ViewBoundAppFragment
import com.w2sv.autocrop.ui.util.navigateAnimatedAndPopCurrentDestination
import com.w2sv.autocrop.ui.screen.cropSessionInjectedViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import com.w2sv.core.common.R.string as Strings

@AndroidEntryPoint
class SaveAllFragment : ViewBoundAppFragment<SaveAllBinding>(SaveAllBinding::class.java) {

    private val viewModel by cropSessionInjectedViewModel<SaveAllViewModel, SaveAllViewModel.Factory>()

    override val onBackPressed: () -> Unit
        get() = { requireContext().showToast(getString(Strings.wait_until_crops_have_been_saved)) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.progress.observe(viewLifecycleOwner) {
            binding.progressTv.updateText(
                minOf(it + 1, viewModel.remainingBundleCount),
                viewModel.remainingBundleCount
            )
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                viewModel.processBundles(
                    context = requireContext(),
                    onFinished = { navController.navigateAnimatedAndPopCurrentDestination(SaveAllFragmentDirections.navigateToExitScreen()) }
                )
            }
        }
    }
}
