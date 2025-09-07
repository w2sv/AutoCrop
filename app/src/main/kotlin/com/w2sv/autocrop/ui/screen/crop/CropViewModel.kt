package com.w2sv.autocrop.ui.screen.crop

import android.content.ContentResolver
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.w2sv.androidutils.lifecycle.increment
import com.w2sv.autocrop.CropNavGraphArgs
import com.w2sv.autocrop.ui.screen.CropSessionAccessingViewModelFactory
import com.w2sv.autocrop.ui.util.nonNullValue
import com.w2sv.cropping.cropping.createCropBundle
import com.w2sv.domain.model.CropBundle
import com.w2sv.domain.repository.PreferencesRepository
import com.w2sv.domain.session.CropSession
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.withContext
import slimber.log.i

@HiltViewModel(assistedFactory = CropViewModel.Factory::class)
class CropViewModel @AssistedInject constructor(
    savedStateHandle: SavedStateHandle,
    preferencesRepository: PreferencesRepository,
    @Assisted private val cropSession: CropSession
) : ViewModel() {

    private val screenshotUris: List<Uri> = CropNavGraphArgs.fromSavedStateHandle(savedStateHandle).imageUris.toList()
    val screenshotCount = screenshotUris.size

    val cropProgress: LiveData<Int> get() = _cropProgress
    private val _cropProgress = MutableLiveData(0)

    private val imminentUris: List<Uri>
        get() = screenshotUris.run {
            subList(cropProgress.nonNullValue, size)
        }

    private val cropSensitivity = preferencesRepository.cropSensitivity.stateIn(viewModelScope, SharingStarted.Eagerly)

    suspend fun cropScreenshots(
        contentResolver: ContentResolver,
        onAnySuccessfulCrops: () -> Unit,
        onNoSuccessfulCrops: () -> Unit
    ) {
        imminentUris.forEach { uri ->
            withContext(Dispatchers.IO) {
                attemptCropBundleCreation(uri, contentResolver)
            }
            _cropProgress.increment()
        }

        if (cropSession.bundles.isNotEmpty()) {
            onAnySuccessfulCrops()
        } else {
            onNoSuccessfulCrops()
        }
    }

    private fun attemptCropBundleCreation(screenshotUri: Uri, contentResolver: ContentResolver) {
        i { "attemptCropBundleCreation; screenshotUri=$screenshotUri" }

        return createCropBundle(
            screenshotMediaUri = screenshotUri,
            cropSensitivity = cropSensitivity.value,
            contentResolver = contentResolver
        )
            .run {
                when (this) {
                    is CropBundle.CreationResult.NoCropEdgesFound -> cropSession.addUncroppableImage(screenshotUri)
                    is CropBundle.CreationResult.BitmapLoadingFailed -> cropSession.addUnopenableImage(screenshotUri)
                    is CropBundle.CreationResult.Success -> cropSession.add(cropBundle)
                }
            }
    }

    @AssistedFactory
    interface Factory : CropSessionAccessingViewModelFactory<CropViewModel>
}
