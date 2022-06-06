package com.lyrebirdstudio.croppylib.ui

import android.app.Application
import android.graphics.RectF
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.lyrebirdstudio.croppylib.main.CropRequest
import com.lyrebirdstudio.croppylib.state.CropFragmentViewState
import com.lyrebirdstudio.croppylib.util.bitmap.BitmapUtils
import com.lyrebirdstudio.croppylib.util.bitmap.ResizedBitmap
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import kotlin.properties.Delegates

class ImageCropViewModel(private val app: Application) : AndroidViewModel(app) {

    private val compositeDisposable = CompositeDisposable()

    var cropRequest: CropRequest
        get() = _cropRequest!!
        set(value) {
            _cropRequest = value
        }
    private var _cropRequest: CropRequest? by Delegates.observable(null){ _, _, newValue ->
        compositeDisposable.add(
            BitmapUtils
                .resize(newValue!!.sourceUri, app.applicationContext)
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(Consumer { resizedBitmapLiveData.value = it })
        )

        with(cropViewStateLiveData){
            postValue(
                value?.onThemeChanged(croppyTheme = newValue.croppyTheme)
            )
        }
    }

    private val cropViewStateLiveData = MutableLiveData<CropFragmentViewState>()
        .apply {
            value = CropFragmentViewState()
        }

    private val resizedBitmapLiveData = MutableLiveData<ResizedBitmap>()

    fun getCropViewStateLiveData(): LiveData<CropFragmentViewState> = cropViewStateLiveData

    fun getResizedBitmapLiveData(): LiveData<ResizedBitmap> = resizedBitmapLiveData

    fun updateCropSize(cropRect: RectF) {
        cropViewStateLiveData.value =
            cropViewStateLiveData.value?.onCropSizeChanged(cropRect = cropRect)
    }

    override fun onCleared() {
        super.onCleared()

        if (compositeDisposable.isDisposed.not()) {
            compositeDisposable.dispose()
        }
    }
}