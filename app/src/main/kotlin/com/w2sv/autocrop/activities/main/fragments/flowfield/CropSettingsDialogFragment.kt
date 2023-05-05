package com.w2sv.autocrop.activities.main.fragments.flowfield

import androidx.fragment.app.viewModels
import com.w2sv.androidutils.lifecycle.postValue
import com.w2sv.androidutils.notifying.showToast
import com.w2sv.autocrop.R
import com.w2sv.autocrop.ui.AbstractCropSettingsDialogFragment
import com.w2sv.common.datastore.DataStoreRepository
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@AndroidEntryPoint
class CropSettingsDialogFragment : AbstractCropSettingsDialogFragment(
    R.string.crop_settings,
    R.drawable.ic_settings_24,
    R.string.apply
) {

    @HiltViewModel
    class ViewModel @Inject constructor(private val dataStoreRepository: DataStoreRepository) : AbstractCropSettingsDialogFragment.ViewModel(
        dataStoreRepository.edgeCandidateThreshold.value
    ) {
        fun syncCropSettings() {
            dataStoreRepository.edgeCandidateThreshold.value = edgeCandidateThreshold
            settingsDissimilarLive.postValue(false)
        }
    }

    override val viewModel by viewModels<ViewModel>()

    override fun onPositiveButtonClicked() {
        viewModel.syncCropSettings()
        requireContext().showToast("Updated Crop Settings")
    }
}