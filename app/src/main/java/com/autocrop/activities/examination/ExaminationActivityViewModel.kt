package com.autocrop.activities.examination

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import com.autocrop.dataclasses.CropBundle
import com.autocrop.utils.kotlin.delegates.AutoSwitch
import com.autocrop.utils.android.documentUriPathIdentifier
import com.autocrop.utils.android.externalPicturesDir
import kotlinx.coroutines.Job
import timber.log.Timber

class ExaminationActivityViewModel(private val validSaveDirDocumentUri: Uri?, val nDismissedScreenshots: Int)
    : ViewModel() {

    companion object{
        lateinit var cropBundles: MutableList<CropBundle>
    }

    /**
     * Clear [cropBundles]
     */
    override fun onCleared() {
        super.onCleared()

        cropBundles.clear()
    }

    var displayedDismissedScreenshotsSnackbar by AutoSwitch(false, switchOn = false)

    var singularCropSavingJob: Job? = null

    var nDeletedScreenshots = 0

    val savedCropUris = mutableListOf<Uri>()
    val deletionQueryUris = mutableListOf<Uri>()

    fun processCropBundle(cropBundlesPosition: Int, deleteScreenshot: Boolean, context: Context){
        val (savingResult, deletionResult) = context.processCropBundle(
            cropBundles[cropBundlesPosition],
            validSaveDirDocumentUri,
            deleteScreenshot
        )

        savingResult.let{ (successful, uri) ->
            if (successful)
                savedCropUris.add(uri)
        }

        deletionResult?.let { (successful, uri) ->
            if (uri != null){
                deletionQueryUris.add(uri)
                Timber.i("Added $uri to deletionQueryScreenshotUris")
            }
            else if (successful)
                nDeletedScreenshots++
        }
    }

    fun cropWriteDirIdentifier(): String =
        validSaveDirDocumentUri?.let {
            documentUriPathIdentifier(it)
        } ?: externalPicturesDir.path
}