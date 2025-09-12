package com.w2sv.autocrop.ui.screen.saveall

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.w2sv.androidutils.lifecycle.increment
import com.w2sv.autocrop.ui.screen.CropSessionAccessingViewModelFactory
import com.w2sv.cropping.session.CropSession
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@HiltViewModel(assistedFactory = SaveAllViewModel.Factory::class)
class SaveAllViewModel @AssistedInject constructor(
    @Assisted private val cropSession: CropSession
) : ViewModel() {

    val remainingBundleCount = cropSession.bundles.value.size

    val progress: LiveData<Int> get() = _progress
    private val _progress = MutableLiveData(0)

    suspend fun processBundles(context: Context, onFinished: () -> Unit) {
        cropSession.processAllBundles(
            context = context,
            onBundleProcessed = {
                withContext(Dispatchers.Main) {
                    _progress.increment()
                }
            }
        )
        onFinished()
    }

    @AssistedFactory
    interface Factory : CropSessionAccessingViewModelFactory<SaveAllViewModel>
}
