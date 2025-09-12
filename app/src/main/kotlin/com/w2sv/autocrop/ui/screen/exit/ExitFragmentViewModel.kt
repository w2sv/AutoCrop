package com.w2sv.autocrop.ui.screen.exit

import androidx.lifecycle.ViewModel
import com.w2sv.autocrop.ui.screen.CropSessionAccessingViewModelFactory
import com.w2sv.cropping.session.CropSession
import com.w2sv.domain.model.CropBundleProcessingResult
import com.w2sv.domain.model.Screenshot
import com.w2sv.kotlinutils.threadUnsafeLazy
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel

@HiltViewModel(assistedFactory = ExitFragmentViewModel.Factory::class)
class ExitFragmentViewModel @AssistedInject constructor(
    @Assisted private val cropSession: CropSession
) : ViewModel() {

    val deletionApprovalRequiringCropBundleProcessingResults: List<CropBundleProcessingResult> by lazy {
        cropSession.cropBundleProcessingResults.filter {
            it.screenshotDeletionResult is Screenshot.DeletionResult.ApprovalRequired
        }
    }

    @AssistedFactory
    interface Factory : CropSessionAccessingViewModelFactory<ExitFragmentViewModel>
}
