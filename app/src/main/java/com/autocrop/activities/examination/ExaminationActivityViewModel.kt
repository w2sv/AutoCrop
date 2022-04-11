package com.autocrop.activities.examination

import android.net.Uri
import androidx.lifecycle.ViewModel
import com.autocrop.types.CropBundle
import com.autocrop.utils.android.parentDirPath

class ExaminationActivityViewModel(val nDismissedImages: Int): ViewModel() {

    companion object{
        lateinit var cropBundles: MutableList<CropBundle>
    }

    private var _nSavedCrops = 0
    val nSavedCrops: Int
        get() = _nSavedCrops

    private var _nDeletedScreenshots = 0
    val nDeletedScreenshots: Int
        get() = _nDeletedScreenshots

    fun incrementImageFileIOCounters(deletedScreenshot: Boolean){
        _nSavedCrops++
        if (deletedScreenshot)
            _nDeletedScreenshots++
    }

    var cropWriteDirPath: String? = null
    fun setCropWriteDirPathIfApplicable(cropWriteUri: Uri?){
        if (cropWriteUri != null && cropWriteDirPath == null)
            cropWriteDirPath = cropWriteUri.parentDirPath
    }
}