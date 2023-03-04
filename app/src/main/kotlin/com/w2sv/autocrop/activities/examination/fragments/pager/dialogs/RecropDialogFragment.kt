package com.w2sv.autocrop.activities.examination.fragments.pager.dialogs

import androidx.fragment.app.viewModels
import androidx.lifecycle.SavedStateHandle
import com.w2sv.autocrop.R
import com.w2sv.autocrop.ui.AbstractCropSettingsDialogFragment
import com.w2sv.autocrop.utils.getFragment
import com.w2sv.cropbundle.CropBundle
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@AndroidEntryPoint
class RecropDialogFragment : AbstractCropSettingsDialogFragment(
    R.string.recrop_with_adjusted_settings,
    R.drawable.ic_autorenew_24,
    R.string.recrop
) {

    companion object {
        private const val EXTRA_INITIAL_THRESHOLD = "com.w2sv.autocrop.extra.INITIAL_THRESHOLD"

        fun getInstance(cropBundlePosition: Int, initialThreshold: Int): RecropDialogFragment =
            getFragment(
                RecropDialogFragment::class.java,
                CropBundle.EXTRA_POSITION to cropBundlePosition,
                EXTRA_INITIAL_THRESHOLD to initialThreshold
            )
    }

    @HiltViewModel
    class ViewModel @Inject constructor(savedStateHandle: SavedStateHandle) : AbstractCropSettingsDialogFragment.ViewModel(
        savedStateHandle[EXTRA_INITIAL_THRESHOLD]!!
    ) {
        val cropBundlePosition: Int = savedStateHandle[CropBundle.EXTRA_POSITION]!!
    }

    override val viewModel by viewModels<ViewModel>()

    override fun onPositiveButtonClicked() {
        (requireParentFragment() as Listener).onDoRecrop(
            viewModel.cropBundlePosition,
            viewModel.edgeCandidateThreshold.toDouble()
        )
    }

    interface Listener {
        fun onDoRecrop(cropBundlePosition: Int, threshold: Double)
    }
}