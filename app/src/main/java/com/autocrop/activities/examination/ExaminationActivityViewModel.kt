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

    var singleCropSavingJob: Job? = null

    var nSavedCrops = 0
    var nDeletedScreenshots = 0

    val cropSavingUris = mutableListOf<Uri>()
    val deletionQueryScreenshotUris = mutableListOf<Uri>()

    fun processCropBundle(cropBundlesPosition: Int, deleteScreenshot: Boolean, context: Context){
        val (savingResult, deletionResult) = context.processCropBundle(
            cropBundles[cropBundlesPosition],
            validSaveDirDocumentUri,
            deleteScreenshot
        )

        savingResult.let{ (successful, uri) ->
            if (successful) {
                cropSavingUris.add(uri)
                nSavedCrops++
            }
        }

        deletionResult?.let { (successful, uri) ->
            uri?.let {
                deletionQueryScreenshotUris.add(it)
                Timber.i("Added $it to deletionQueryScreenshotUris")
            }
            if (successful)
                nDeletedScreenshots++
        }
    }

    fun cropWriteDirIdentifier(): String =
        validSaveDirDocumentUri?.let {
            documentUriPathIdentifier(it)
        } ?: externalPicturesDir.path
}