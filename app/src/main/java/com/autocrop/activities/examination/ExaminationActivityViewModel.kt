package com.autocrop.activities.examination

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import com.autocrop.collections.CropBundle
import com.autocrop.utils.android.externalPicturesDir
import timber.log.Timber

class ExaminationActivityViewModel(private val validSaveDirDocumentUri: Uri?)
    : ViewModel() {

    companion object{
        lateinit var cropBundles: MutableList<CropBundle>
    }

    val nSavedCrops: Int
        get() = _nSavedCrops
    private var _nSavedCrops = 0

    val nDeletedScreenshots: Int
        get() = _nDeletedScreenshots
    private var _nDeletedScreenshots = 0

    val cropSavingUris = mutableListOf<Uri>()

    fun processCropBundle(cropBundlesPosition: Int, deleteScreenshot: Boolean, context: Context){
        val (savingResult, deletionResult) = context.processCropBundle(
            cropBundles[cropBundlesPosition],
            validSaveDirDocumentUri,
            deleteScreenshot
        )
        with(savingResult){
            if (first)
                cropSavingUris.add(second)
        }
        deletionResult?.second?.let {
            deletionQueryScreenshotUris.add(it)
            Timber.i("Added $it to deletionQueryScreenshotUris")
        }

        incrementImageFileIOCounters(deletionResult?.first ?: false)
    }

    private fun incrementImageFileIOCounters(deletedScreenshot: Boolean){
        _nSavedCrops++
        if (deletedScreenshot)
            _nDeletedScreenshots++
    }

    val deletionQueryScreenshotUris: MutableList<Uri> = mutableListOf()
    fun incrementNDeletedScreenshotsByDeletionQueryScreenshotUris(){
        _nDeletedScreenshots += deletionQueryScreenshotUris.size
    }

    fun cropWriteDirIdentifier(): String =
        validSaveDirDocumentUri?.let {
            it.pathSegments[1]
        } ?: externalPicturesDir.path

    /**
     * Clear [cropBundles]
     */
    override fun onCleared() {
        super.onCleared()

        cropBundles.clear()
    }
}