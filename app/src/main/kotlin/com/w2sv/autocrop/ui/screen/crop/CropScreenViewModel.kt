package com.w2sv.autocrop.ui.screen.crop

import android.content.ContentResolver
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.w2sv.androidutils.BackPressHandler
import com.w2sv.androidutils.lifecycle.increment
import com.w2sv.autocrop.model.CropResults
import com.w2sv.autocrop.ui.util.Constant
import com.w2sv.cropbundle.CropBundle
import com.w2sv.domain.repository.PreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.withContext
import slimber.log.i
import javax.inject.Inject

@HiltViewModel
class CropScreenViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    preferencesRepository: PreferencesRepository
) : androidx.lifecycle.ViewModel() {

    val backPressListener = BackPressHandler(
        coroutineScope = viewModelScope,
        confirmationWindowDuration = Constant.BACKPRESS_CONFIRMATION_WINDOW_DURATION
    )

    private val screenshotUris: List<Uri> =
        CropScreenFragmentArgs.fromSavedStateHandle(savedStateHandle).imageUris.toList()
    val nScreenshots = screenshotUris.size

    val cropBundles = mutableListOf<CropBundle>()
    val cropResults = CropResults()

    val liveProgress: LiveData<Int> get() = _liveProgress
    private val _liveProgress = MutableLiveData(0)

    suspend fun cropCoroutine(
        contentResolver: ContentResolver,
        onFinishedListener: suspend () -> Unit
    ) {
        coroutineScope {
            getImminentUris().forEach { uri ->
                withContext(Dispatchers.IO) {
                    attemptCropBundleCreation(uri, contentResolver)?.let {
                        cropBundles.add(it)
                    }
                }
                withContext(Dispatchers.Main) {
                    _liveProgress.increment()
                }
            }
            onFinishedListener()
        }
    }

    private fun getImminentUris(): List<Uri> =
        screenshotUris.run {
            subList(liveProgress.value!!, size)
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
                        cropResults.uncroppableImageCount += 1
                        null
                    }

                    is CropBundle.CreationResult.Failure.BitmapLoadingFailed -> {
                        cropResults.nNotOpenableImages += 1
                        null
                    }

                    is CropBundle.CreationResult.Success -> {
                        cropBundle
                    }
                }
            }
    }

    private val cropSensitivity =
        preferencesRepository.cropSensitivity.stateIn(viewModelScope, SharingStarted.Eagerly)
}