package com.w2sv.autocrop.ui.screen.comparison

import android.content.ContentResolver
import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.w2sv.androidutils.lifecycle.repostValue
import com.w2sv.autocrop.ui.screen.CropSessionAccessingViewModelFactory
import com.w2sv.cropping.session.CropSession
import com.w2sv.domain.model.CropBundle
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel

@HiltViewModel(assistedFactory = ComparisonViewModel.Factory::class)
class ComparisonViewModel @AssistedInject constructor(
    contentResolver: ContentResolver,
    savedStateHandle: SavedStateHandle,
    @Assisted cropSession: CropSession
) : ViewModel() {

    private val cropBundle: CropBundle =
        cropSession.bundles.value[ComparisonFragmentArgs.fromSavedStateHandle(savedStateHandle).cropBundleIndex]
    val crop = cropBundle.crop
    val screenshotBitmap: Bitmap = cropBundle.screenshot.getBitmap(contentResolver)

    val imageType: LiveData<ImageType> get() = _imageType
    private val _imageType = MutableLiveData(ImageType.Crop)

    fun setImageType(value: ImageType) {
        _imageType.value = value
    }

    fun repostImageType() {
        _imageType.repostValue()
    }

    @AssistedFactory
    interface Factory : CropSessionAccessingViewModelFactory<ComparisonViewModel>
}
