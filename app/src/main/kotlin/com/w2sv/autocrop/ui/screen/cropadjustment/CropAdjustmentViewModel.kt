package com.w2sv.autocrop.ui.screen.cropadjustment

import android.content.ContentResolver
import android.graphics.Bitmap
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.w2sv.autocrop.ui.screen.CropSessionAccessingViewModelFactory
import com.w2sv.autocrop.ui.screen.cropadjustment.model.AdjustmentModeState
import com.w2sv.autocrop.ui.screen.cropadjustment.model.AdjustmentViewState
import com.w2sv.autocrop.ui.util.transformedMutableStateIn
import com.w2sv.cropping.cropping.crop
import com.w2sv.cropping.session.CropSession
import com.w2sv.domain.model.CropAdjustmentMode
import com.w2sv.domain.model.CropBundle
import com.w2sv.domain.repository.PreferencesRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = CropAdjustmentViewModel.Factory::class)
class CropAdjustmentViewModel @AssistedInject constructor(
    savedStateHandle: SavedStateHandle,
    contentResolver: ContentResolver,
    private val preferencesRepository: PreferencesRepository,
    @Assisted private val cropSession: CropSession
) : ViewModel() {

    private val bundleIndex = CropAdjustmentFragmentArgs.fromSavedStateHandle(savedStateHandle).cropBundleIndex
    private val cropBundle: CropBundle = cropSession.bundles.value[bundleIndex]
    private val originalEdges by cropBundle.crop::edges
    val screenshotBitmap: Bitmap = cropBundle.screenshot.getBitmap(contentResolver)

    private val _viewState = preferencesRepository
        .cropAdjustmentMode
        .transformedMutableStateIn(viewModelScope) { mode ->
            AdjustmentViewState(
                originalEdges = originalEdges,
                modeState = mode.state()
            )
        }
    val viewState = _viewState.asStateFlow()

    fun updateAdjustmentMode(mode: CropAdjustmentMode) {
        updateAdjustmentModeState(mode.state())
    }

    private fun CropAdjustmentMode.state(): AdjustmentModeState =
        when (this) {
            CropAdjustmentMode.Manual -> AdjustmentModeState.Manual(originalEdges)
            CropAdjustmentMode.EdgeSelection -> AdjustmentModeState.EdgeSelection()
        }

    fun updateAdjustmentModeState(state: AdjustmentModeState) {
        _viewState.update { it.copy(modeState = state) }
    }

    //    private val edgeCandidatePoints: FloatArray by lazy {
    //        cropBundle.edgeCandidates.flatMap {
    //            listOf(
    //                0f,
    //                it.toFloat(),
    //                screenshotBitmap.width.toFloat(),
    //                it.toFloat()
    //            )
    //        }
    //            .toFloatArray()
    //    }

    fun applyAdjustedEdges() {
        cropSession.update(
            index = bundleIndex,
            newBundle = cropBundle.copy(
                crop = screenshotBitmap.crop(
                    cropBundle.screenshot.mediaStoreData.diskUsage,
                    checkNotNull(viewState.value.adjustedEdges)
                )
            )
        )
    }

    override fun onCleared() {
        viewModelScope.launch {
            preferencesRepository.cropAdjustmentMode.save(viewState.value.modeState.mode)
        }
    }

    @AssistedFactory
    interface Factory : CropSessionAccessingViewModelFactory<CropAdjustmentViewModel>
}
