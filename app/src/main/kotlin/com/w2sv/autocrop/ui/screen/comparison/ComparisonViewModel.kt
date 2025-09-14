package com.w2sv.autocrop.ui.screen.comparison

import android.content.ContentResolver
import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.w2sv.autocrop.ui.screen.CropSessionAccessingViewModelFactory
import com.w2sv.autocrop.ui.screen.comparison.views.FadeOutTextView
import com.w2sv.cropping.session.CropSession
import com.w2sv.domain.model.CropBundle
import com.w2sv.kotlinutils.coroutines.flow.emit
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

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

    private val _fadeOutTextArgs = MutableSharedFlow<FadeOutTextView.Args>()
    val fadeOutTextArgs: SharedFlow<FadeOutTextView.Args> = _fadeOutTextArgs.asSharedFlow()

    fun emitFadeOutTextArgs(args: FadeOutTextView.Args) {
        _fadeOutTextArgs.emit(args, viewModelScope)
    }

    val imageType: LiveData<ImageType> get() = _imageType
    private val _imageType = MutableLiveData(ImageType.Crop)

    fun setImageType(value: ImageType, displayFadeOutText: Boolean = true) {
        _imageType.value = value
        if (displayFadeOutText) {
            emitFadeOutTextArgs(FadeOutTextView.Args(value.labelRes))
        }
    }

    @AssistedFactory
    interface Factory : CropSessionAccessingViewModelFactory<ComparisonViewModel>
}
