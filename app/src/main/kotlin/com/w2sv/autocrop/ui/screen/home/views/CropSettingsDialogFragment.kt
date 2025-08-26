package com.w2sv.autocrop.ui.screen.home.views

import androidx.fragment.app.viewModels
import androidx.lifecycle.viewModelScope
import com.w2sv.androidutils.widget.showToast
import com.w2sv.autocrop.R
import com.w2sv.autocrop.ui.designsystem.AbstractCropSettingsDialogFragment
import com.w2sv.core.common.R.string as Strings
import com.w2sv.domain.repository.PreferencesRepository
import com.w2sv.kotlinutils.coroutines.flow.firstBlocking
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CropSettingsDialogFragment :
    AbstractCropSettingsDialogFragment(
        title = Strings.crop_settings,
        icon = R.drawable.ic_settings_24,
        positiveButtonText = Strings.apply
    ) {
    override val viewModel by viewModels<ViewModel>()

    override fun onPositiveButtonClicked() {
        viewModel.syncCropSettings()
        requireContext().showToast(Strings.updated_crop_settings)
    }

    @HiltViewModel
    class ViewModel @Inject constructor(private val preferencesRepository: PreferencesRepository) :
        AbstractCropSettingsDialogFragment.ViewModel(
            preferencesRepository.cropSensitivity.firstBlocking() // TODO
        ) {
        fun syncCropSettings() {
            viewModelScope.launch { preferencesRepository.cropSensitivity.save(cropSensitivity.value!!) }
            sensitivityHasChangedMutable.postValue(false)
        }
    }
}
