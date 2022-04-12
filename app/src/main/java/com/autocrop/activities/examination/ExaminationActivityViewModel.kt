package com.autocrop.activities.examination

import android.net.Uri
import androidx.lifecycle.ViewModel
import com.autocrop.global.CropFileSaveDestinationPreferences
import com.autocrop.types.CropBundle
import com.autocrop.utils.android.externalPicturesDir

class ExaminationActivityViewModel(val nDismissedImages: Int, val documentUriWritePermissionValid: Boolean?)
    : ViewModel() {

    companion object{
        lateinit var cropBundles: MutableList<CropBundle>
    }

    private var _nSavedCrops = 0
    val nSavedCrops: Int
        get() = _nSavedCrops

    private var _nDeletedScreenshots = 0
    val nDeletedScreenshots: Int
        get() = _nDeletedScreenshots

    val deletionQueryScreenshotUris: MutableList<Uri> = mutableListOf()

    fun incrementImageFileIOCounters(deletedScreenshot: Boolean){
        _nSavedCrops++
        if (deletedScreenshot)
            _nDeletedScreenshots++
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