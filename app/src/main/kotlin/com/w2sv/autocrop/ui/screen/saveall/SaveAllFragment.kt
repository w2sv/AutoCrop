package com.w2sv.autocrop.ui.screen.saveall

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.w2sv.androidutils.widget.showToast
import com.w2sv.autocrop.AppFragment
import com.w2sv.autocrop.R
import com.w2sv.autocrop.databinding.SaveAllBinding
import com.w2sv.autocrop.ui.screen.CropBundleViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SaveAllFragment :
    AppFragment<SaveAllBinding>(SaveAllBinding::class.java) {

    private val examinationVM by activityViewModels<CropBundleViewModel>()

    override val onBackPressed: () -> Unit
        get() = { requireContext().showToast(getString(R.string.wait_until_crops_have_been_saved)) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        examinationVM.saveAllProgress.observe(viewLifecycleOwner) {
            binding.progressTv.updateText(
                minOf(it + 1, examinationVM.nUnprocessedCrops),
                examinationVM.nUnprocessedCrops
            )
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                examinationVM.saveAllCoroutine(
                    context = requireContext(),
                    onFinishedListener = { navController.navigate(SaveAllFragmentDirections.navigateToExitScreen()) }
                )
            }
        }
    }
}