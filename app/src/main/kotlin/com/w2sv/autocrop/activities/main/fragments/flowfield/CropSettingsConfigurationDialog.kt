package com.w2sv.autocrop.activities.main.fragments.flowfield

import androidx.fragment.app.viewModels
import com.w2sv.androidutils.extensions.postValue
import com.w2sv.androidutils.extensions.showToast
import com.w2sv.autocrop.R
import com.w2sv.autocrop.ui.CropSettingsDialog
import com.w2sv.preferences.IntPreferences
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@AndroidEntryPoint
class CropSettingsConfigurationDialog : CropSettingsDialog(R.string.crop_settings, R.string.apply) {

    @HiltViewModel
    class ViewModel @Inject constructor(private val intPreferences: IntPreferences) : CropSettingsDialog.ViewModel(
        intPreferences.edgeCandidateThreshold
    ) {
        fun syncCropSettings() {
            intPreferences.edgeCandidateThreshold = edgeCandidateThreshold
            settingsDissimilarLive.postValue( false)
        }
    }

    override val viewModel by viewModels<ViewModel>()

    override fun onPositiveButtonClicked() {
        viewModel.syncCropSettings()
        requireContext().showToast("Updated Crop Settings")
    }
}