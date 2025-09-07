package com.w2sv.autocrop.ui.screen.pager

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.w2sv.autocrop.ui.screen.CropSessionAccessingViewModelFactory
import com.w2sv.cropping.io.CropBundleIOProcessingUseCase
import com.w2sv.domain.repository.PreferencesRepository
import com.w2sv.domain.session.CropSession
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = CropInspectionViewModel.Factory::class)
class CropInspectionViewModel @AssistedInject constructor(
    private val cropBundleIOProcessingUseCase: CropBundleIOProcessingUseCase,
    private val preferencesRepository: PreferencesRepository,
    @Assisted private val cropSession: CropSession
) : ViewModel() {

    val cropBundles by cropSession::bundles

    fun discardCropBundleAt(index: Int) {
        cropSession.removeAt(index)
    }

    val deleteScreenshots = preferencesRepository.deleteScreenshots.stateIn(viewModelScope, SharingStarted.Eagerly)

    fun toggleDeleteScreenshots() {
        viewModelScope.launch { preferencesRepository.deleteScreenshots.save(!deleteScreenshots.value) }
    }

    fun processCropBundleAt(index: Int, context: Context) {
        val cropBundle = cropBundles[index]
        cropProcessingJob = viewModelScope.launch(Dispatchers.IO) {
            cropSession.addCropBundleProcessingResult(
                cropBundleIOProcessingUseCase.invoke(
                    cropBitmap = cropBundle.crop.bitmap,
                    screenshotMediaStoreData = cropBundle.screenshot.mediaStoreData,
                    deleteScreenshot = deleteScreenshots.value,
                    context = context
                )
            )
        }
    }

    var cropProcessingJob: Job? = null
        private set

    @AssistedFactory
    interface Factory : CropSessionAccessingViewModelFactory<CropInspectionViewModel>
}
