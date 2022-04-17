package com.autocrop.activities.examination

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import com.autocrop.global.CropFileSaveDestinationPreferences
import com.autocrop.types.CropBundle
import com.autocrop.utils.android.externalPicturesDir
import timber.log.Timber

class ExaminationActivityViewModel(private val documentUriWritePermissionValid: Boolean?)
    : ViewModel() {

    companion object{
        lateinit var cropBundles: MutableList<CropBundle>
    }

    fun processCropBundle(cropBundlesPosition: Int, deleteScreenshot: Boolean, context: Context){
        val (_, deletionResult) = context.processCropBundle(
            cropBundles[cropBundlesPosition],
            documentUriWritePermissionValid,
            deleteScreenshot
        )
        deletionResult?.second?.let {
            deletionQueryScreenshotUris.add(it)
            Timber.i("Added $it to deletionQueryScreenshotUris")
        }

        incrementImageFileIOCounters(deletionResult?.first ?: false)
    }

    private var _nSavedCrops = 0
    val nSavedCrops: Int
        get() = _nSavedCrops

    private var _nDeletedScreenshots = 0
    val nDeletedScreenshots: Int
        get() = _nDeletedScreenshots

    val deletionQueryScreenshotUris: MutableList<Uri> = mutableListOf()

    private fun incrementImageFileIOCounters(deletedScreenshot: Boolean){
        _nSavedCrops++
        if (deletedScreenshot)
            _nDeletedScreenshots++
    }
    fun incrementNDeletedScreenshotsByDeletionQueryScreenshotUris(){
        _nDeletedScreenshots += deletionQueryScreenshotUris.size
    }

    fun cropWriteDirIdentifier(): String =
        if (documentUriWritePermissionValid == true)
            CropFileSaveDestinationPreferences.documentUri!!.pathSegments[1]
        else
            externalPicturesDir.parent!!

    /**
     * Clear [cropBundles]
     */
    override fun onCleared() {
        super.onCleared()

        cropBundles.clear()
    }
}