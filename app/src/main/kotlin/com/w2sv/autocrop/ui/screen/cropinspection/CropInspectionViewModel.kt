package com.w2sv.autocrop.ui.screen.cropinspection

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.w2sv.autocrop.ui.screen.CropSessionAccessingViewModelFactory
import com.w2sv.cropping.session.CropSession
import com.w2sv.domain.repository.PreferencesRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = CropInspectionViewModel.Factory::class)
class CropInspectionViewModel @AssistedInject constructor(
    private val preferencesRepository: PreferencesRepository,
    @Assisted private val cropSession: CropSession
) : ViewModel() {

    val cropBundles by cropSession::bundles

    fun discardCropBundleAt(index: Int) {
        cropSession.removeBundleAt(index)
    }

    fun processCropBundleAt(index: Int, context: Context) {
        viewModelScope.launch { cropSession.processCropBundleAt(index, context) }
    }

    val deleteScreenshots = preferencesRepository.deleteScreenshots.stateIn(viewModelScope, SharingStarted.Eagerly)

    fun toggleDeleteScreenshots() {
        viewModelScope.launch { preferencesRepository.deleteScreenshots.save(!deleteScreenshots.value) }
    }

    @AssistedFactory
    interface Factory : CropSessionAccessingViewModelFactory<CropInspectionViewModel>
}
