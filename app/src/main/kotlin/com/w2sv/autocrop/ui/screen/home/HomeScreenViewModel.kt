package com.w2sv.autocrop.ui.screen.home

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.w2sv.androidutils.lifecycle.toggle
import com.w2sv.androidutils.widget.showToast
import com.w2sv.autocrop.model.CropBundleIOResults
import com.w2sv.autocrop.util.cropSaveDirPathIdentifier
import com.w2sv.domain.repository.PreferencesRepository
import com.w2sv.kotlinutils.coroutines.mapState
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeScreenViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val preferencesRepository: PreferencesRepository,
    //        cancelledSSLFromNotification: ScreenshotListener.CancelledFromNotification,
    @ApplicationContext context: Context,
    private val resources: Resources
) : androidx.lifecycle.ViewModel() {

    val cropBundleIoResults: CropBundleIOResults? = savedStateHandle[CropBundleIOResults.EXTRA]

    var fadedInForegroundOnEntry = false

    /**
     * IO Results Notification
     */

    fun showIOResultsNotificationIfApplicable(
        context: Context
    ) {
        cropBundleIoResults?.let {
            context.showToast(it.getNotificationText(resources))
        }
    }

    /**
     * Misc LiveData
     */

    val hideForegroundElements: LiveData<Boolean> get() = _hideForegroundElements
    private val _hideForegroundElements = MutableLiveData(false)

    fun toggleHideForegroundElements() {
        _hideForegroundElements.toggle()
    }

    val cropSaveDirIdentifier = preferencesRepository.cropSaveDirDocumentUri
        .mapState { cropSaveDirPathIdentifier(it, context) }
        .asLiveData()

    //        val screenshotListenerRunning: LiveData<Boolean> get() = _screenshotListenerRunning
    //        private val _screenshotListenerRunning = MutableLiveData(context.isServiceRunning<ScreenshotListener>())
    //
    //        fun setScreenshotListenerRunning(value: Boolean) {
    //            _screenshotListenerRunning.postValue(value)
    //        }
    //
    //        init {
    //            viewModelScope.collectFromFlow(cancelledSSLFromNotification.sharedFlow) {
    //                setScreenshotListenerRunning(false)
    //            }
    //        }

    fun setCropSaveDirTreeUri(treeUri: Uri, contentResolver: ContentResolver) {
        contentResolver
            .takePersistableUriPermission(
                treeUri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            )
        viewModelScope.launch { preferencesRepository.saveCropSaveDirTreeUri(treeUri) }
    }

    val cropSaveDirTreeUri = preferencesRepository.cropSaveDirTreeUri
}