package com.w2sv.autocrop.ui.screen.pager.dialog.recrop

import androidx.fragment.app.viewModels
import androidx.lifecycle.SavedStateHandle
import com.w2sv.autocrop.R
import com.w2sv.autocrop.ui.designsystem.AbstractCropSettingsDialogFragment
import com.w2sv.autocrop.ui.util.nonNullValue
import com.w2sv.core.common.R.string as Strings
import com.w2sv.domain.model.CropSensitivity
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@AndroidEntryPoint
class RecropDialogFragment :
    AbstractCropSettingsDialogFragment(
        title = Strings.recrop_with_adjusted_settings,
        icon = R.drawable.ic_autorenew_24,
        positiveButtonText = Strings.recrop
    ) {
    override val viewModel by viewModels<ViewModel>()

    override fun onPositiveButtonClicked() {
        (requireParentFragment() as Listener)
            .onRecrop(
                cropSensitivity = viewModel.cropSensitivity.nonNullValue
            )
    }

    @HiltViewModel
    protected class ViewModel @Inject constructor(savedStateHandle: SavedStateHandle) :
        AbstractCropSettingsDialogFragment.ViewModel(
            initialCropSensitivity = RecropDialogFragmentArgs.fromSavedStateHandle(savedStateHandle).initialCropSensitivity
        )

    interface Listener {
        fun onRecrop(@CropSensitivity cropSensitivity: Int)
    }
}
