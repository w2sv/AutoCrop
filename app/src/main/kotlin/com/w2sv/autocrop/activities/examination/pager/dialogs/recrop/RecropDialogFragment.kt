package com.w2sv.autocrop.activities.examination.pager.dialogs.recrop

import androidx.fragment.app.viewModels
import androidx.lifecycle.SavedStateHandle
import com.w2sv.autocrop.R
import com.w2sv.autocrop.ui.AbstractCropSettingsDialogFragment
import com.w2sv.autocrop.utils.getFragment
import com.w2sv.cropbundle.CropBundle
import com.w2sv.cropbundle.cropping.CropSensitivity
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@AndroidEntryPoint
class RecropDialogFragment : AbstractCropSettingsDialogFragment(
    title = R.string.recrop_with_adjusted_settings,
    icon = R.drawable.ic_autorenew_24,
    positiveButtonText = R.string.recrop
) {
    override val viewModel by viewModels<ViewModel>()

    override fun onPositiveButtonClicked() {
        (requireParentFragment() as Listener)
            .onRecrop(
                cropBundlePosition = viewModel.cropBundlePosition,
                cropSensitivity = viewModel.cropSensitivity.value!!
            )
    }

    @HiltViewModel
    class ViewModel @Inject constructor(savedStateHandle: SavedStateHandle) : AbstractCropSettingsDialogFragment.ViewModel(
        initialCropSensitivity = savedStateHandle[EXTRA_INITIAL_THRESHOLD]!!
    ) {
        val cropBundlePosition: Int = savedStateHandle[CropBundle.EXTRA_POSITION]!!
    }

    interface Listener {
        fun onRecrop(cropBundlePosition: Int, @CropSensitivity cropSensitivity: Int)
    }

    companion object {
        private const val EXTRA_INITIAL_THRESHOLD = "com.w2sv.autocrop.extra.INITIAL_THRESHOLD"

        fun getInstance(cropBundlePosition: Int, initialThreshold: Int): RecropDialogFragment =
            getFragment(
                RecropDialogFragment::class.java,
                CropBundle.EXTRA_POSITION to cropBundlePosition,
                EXTRA_INITIAL_THRESHOLD to initialThreshold
            )
    }
}