package com.w2sv.autocrop.ui.screen.comparison

import android.content.ContentResolver
import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.w2sv.androidutils.lifecycle.repostValue
import com.w2sv.autocrop.ui.screen.comparison.model.ImageType
import com.w2sv.cropbundle.CropBundle
import com.w2sv.domain.repository.PreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import javax.inject.Inject

@HiltViewModel
class ComparisonViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    contentResolver: ContentResolver,
    preferencesRepository: PreferencesRepository
) : androidx.lifecycle.ViewModel() {

    val instructionsShown =
        preferencesRepository.comparisonInstructionsShown.stateIn(viewModelScope, SharingStarted.Eagerly)

    val cropBundle: CropBundle = ComparisonFragmentArgs.fromSavedStateHandle(savedStateHandle).cropBundle
    val screenshotBitmap: Bitmap = cropBundle.screenshot.getBitmap(contentResolver)

    var enterTransitionCompleted = false

    val imageType: LiveData<ImageType> get() = _imageType
    private val _imageType = MutableLiveData(ImageType.Crop)

    fun postImageType(value: ImageType) {
        _imageType.postValue(value)
    }

    fun repostImageType() {
        _imageType.repostValue()
    }
}