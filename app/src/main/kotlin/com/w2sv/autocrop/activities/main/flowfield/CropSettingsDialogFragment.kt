package com.w2sv.autocrop.activities.main.flowfield

import androidx.fragment.app.viewModels
import androidx.lifecycle.viewModelScope
import com.w2sv.androidutils.lifecycle.postValue
import com.w2sv.androidutils.notifying.showToast
import com.w2sv.autocrop.R
import com.w2sv.autocrop.ui.AbstractCropSettingsDialogFragment
import com.w2sv.domain.repository.PreferencesRepository
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class CropSettingsDialogFragment : AbstractCropSettingsDialogFragment(
    R.string.crop_settings,
    R.drawable.ic_settings_24,
    R.string.apply
) {

    @HiltViewModel
    class ViewModel @Inject constructor(private val preferencesRepository: PreferencesRepository) : AbstractCropSettingsDialogFragment.ViewModel(
        preferencesRepository.edgeCandidateThreshold
    ) {
        fun syncCropSettings() {
            viewModelScope.launch { preferencesRepository.edgeCandidateThreshold.save(edgeCandidateThreshold) }
            settingsDissimilarLive.postValue(false)
        }
    }

    override val viewModel by viewModels<ViewModel>()

    override fun onPositiveButtonClicked() {
        viewModel.syncCropSettings()
        requireContext().showToast("Updated Crop Settings")
    }
}