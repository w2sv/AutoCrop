package com.w2sv.autocrop.ui.screen.home.views

import androidx.fragment.app.viewModels
import androidx.lifecycle.viewModelScope
import com.w2sv.androidutils.widget.showToast
import com.w2sv.autocrop.R
import com.w2sv.autocrop.ui.views.AbstractCropSettingsDialogFragment
import com.w2sv.domain.repository.PreferencesRepository
import com.w2sv.kotlinutils.coroutines.firstBlocking
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class CropSettingsDialogFragment : AbstractCropSettingsDialogFragment(
    title = R.string.crop_settings,
    icon = R.drawable.ic_settings_24,
    positiveButtonText = R.string.apply
) {
    override val viewModel by viewModels<ViewModel>()

    override fun onPositiveButtonClicked() {
        viewModel.syncCropSettings()
        requireContext().showToast(R.string.updated_crop_settings)
    }

    @HiltViewModel
    class ViewModel @Inject constructor(private val preferencesRepository: PreferencesRepository) : AbstractCropSettingsDialogFragment.ViewModel(
        preferencesRepository.cropSensitivity.firstBlocking()  // TODO
    ) {
        fun syncCropSettings() {
            viewModelScope.launch { preferencesRepository.cropSensitivity.save(cropSensitivity.value!!) }
            _sensitivityHasChanged.postValue(false)
        }
    }
}