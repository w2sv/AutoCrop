package com.w2sv.autocrop.ui.screen.crop

import android.content.ContentResolver
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.w2sv.androidutils.BackPressHandler
import com.w2sv.androidutils.lifecycle.increment
import com.w2sv.autocrop.CropNavGraphArgs
import com.w2sv.autocrop.model.CropResults
import com.w2sv.autocrop.ui.util.Constant
import com.w2sv.autocrop.ui.util.nonNullValue
import com.w2sv.cropbundle.CropBundle
import com.w2sv.domain.repository.PreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.withContext
import slimber.log.i
import javax.inject.Inject

@HiltViewModel
class CropScreenViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    preferencesRepository: PreferencesRepository
) : androidx.lifecycle.ViewModel() {

    private val screenshotUris: List<Uri> =
        CropNavGraphArgs.fromSavedStateHandle(savedStateHandle).imageUris.toList()
    val screenshotCount = screenshotUris.size

    val cropProgress: LiveData<Int> get() = _cropProgress
    private val _cropProgress = MutableLiveData(0)

    private var unopenableImageCount: Int = 0
    private var uncroppableImageCount: Int = 0

    suspend fun cropScreenshots(
        contentResolver: ContentResolver,
        onCropBundle: (CropBundle) -> Unit,
        onFinishedListener: suspend (CropResults) -> Unit
    ) {
        getImminentUris().forEach { uri ->
            withContext(Dispatchers.IO) {
                attemptCropBundleCreation(uri, contentResolver)?.let {
                    onCropBundle(it)
                }
            }
            withContext(Dispatchers.Main) {
                _cropProgress.increment()
            }
        }
        onFinishedListener(
            CropResults(
                unopenableImageCount = unopenableImageCount,
                uncroppableImageCount = uncroppableImageCount
            )
        )
    }

    private fun getImminentUris(): List<Uri> =
        screenshotUris.run {
            subList(cropProgress.nonNullValue, size)
        }

    private fun attemptCropBundleCreation(screenshotUri: Uri, contentResolver: ContentResolver): CropBundle? {
        i { "attemptCropBundleCreation; screenshotUri=$screenshotUri" }

        return CropBundle.attemptCreation(
            screenshotMediaUri = screenshotUri,
            cropSensitivity = cropSensitivity.value,
            contentResolver = contentResolver
        )
            .run {
                when (this) {
                    is CropBundle.CreationResult.Failure.NoCropEdgesFound -> {
                        uncroppableImageCount += 1
                        null
                    }

                    is CropBundle.CreationResult.Failure.BitmapLoadingFailed -> {
                        unopenableImageCount
                        null
                    }

                    is CropBundle.CreationResult.Success -> {
                        cropBundle
                    }
                }
            }
    }

    val backPressListener = BackPressHandler(
        coroutineScope = viewModelScope,
        confirmationWindowDuration = Constant.BACKPRESS_CONFIRMATION_WINDOW_DURATION
    )

    private val cropSensitivity =
        preferencesRepository.cropSensitivity.stateIn(viewModelScope, SharingStarted.Eagerly)
}