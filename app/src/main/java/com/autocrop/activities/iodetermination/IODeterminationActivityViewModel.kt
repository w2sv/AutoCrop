package com.autocrop.activities.iodetermination

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import com.autocrop.CropBundle
import com.autocrop.Screenshot
import com.autocrop.utils.android.documentUriPathIdentifier
import com.autocrop.utils.android.systemPicturesDirectory
import com.autocrop.utils.kotlin.BlankFun
import com.autocrop.utils.kotlin.delegates.AutoSwitch
import com.autocrop.utils.kotlin.extensions.toInt
import kotlinx.coroutines.Job

class IODeterminationActivityViewModel(private val validSaveDirDocumentUri: Uri?, val nDismissedScreenshots: Int)
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
    val screenshotDeletionInquiryUris = mutableListOf<Uri>()

    fun makeCropBundleProcessor(cropBundlesPosition: Int,
                                deleteScreenshot: Boolean,
                                context: Context): BlankFun {
        val cropBundle = cropBundles[cropBundlesPosition]

        val addedScreenshotDeletionInquiryUri = addScreenshotDeleteRequestUri(
            deleteScreenshot,
            cropBundle.screenshot
        )

        return {
            val ioResult = context.contentResolver.carryOutCropIO(
                cropBundle.crop.bitmap,
                cropBundle.screenshot.mediaStoreData,
                validSaveDirDocumentUri,
                deleteScreenshot && !addedScreenshotDeletionInquiryUri
            )

            if (ioResult.successfullySavedCrop)
                savedCropUris.add(ioResult.writeUri!!)
            nDeletedScreenshots += (ioResult.deletedScreenshot == true).toInt()
        }
    }

    private fun addScreenshotDeleteRequestUri(deleteScreenshot: Boolean,
                                              screenshot: Screenshot
    ): Boolean{
        if (deleteScreenshot)
            deleteRequestUri(screenshot.mediaStoreData.id)?.let {
                screenshotDeletionInquiryUris.add(it)
                return true
            }
        return false
    }

    fun cropWriteDirIdentifier(): String =
        validSaveDirDocumentUri?.let {
            documentUriPathIdentifier(it)
        } ?: systemPicturesDirectory().path
}