package com.w2sv.autocrop.activities.examination.pager.dialogs.recrop

import androidx.fragment.app.viewModels
import com.w2sv.autocrop.R
import com.w2sv.autocrop.ui.AbstractCropSettingsDialogFragment
import com.w2sv.common.datastore.Repository
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@AndroidEntryPoint
class RecropAllDialogFragment : AbstractCropSettingsDialogFragment(
    R.string.recrop_all_with_adjusted_settings,
    R.drawable.ic_autorenew_24,
    R.string.recrop
) {

    @HiltViewModel
    class ViewModel @Inject constructor(repository: Repository) : AbstractCropSettingsDialogFragment.ViewModel(
        repository.edgeCandidateThreshold.value
    )

    override val viewModel by viewModels<ViewModel>()

    override fun onPositiveButtonClicked() {
        (requireParentFragment() as Listener).onRecropAll(viewModel.edgeCandidateThreshold.toDouble())
    }

    interface Listener {
        fun onRecropAll(threshold: Double)
    }
}