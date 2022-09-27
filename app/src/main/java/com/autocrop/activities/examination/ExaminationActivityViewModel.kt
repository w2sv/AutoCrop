package com.autocrop.activities.examination

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import com.autocrop.dataclasses.CropBundle
import com.autocrop.utils.android.documentUriPathIdentifier
import com.autocrop.utils.android.externalPicturesDir
import com.autocrop.utils.kotlin.BlankFun
import com.autocrop.utils.kotlin.delegates.AutoSwitch
import kotlinx.coroutines.Job

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
    val screenshotDeletionInquiryUris = mutableListOf<Uri>()

    fun makeCropBundleProcessor(cropBundlesPosition: Int,
                                deleteScreenshot: Boolean,
                                context: Context): BlankFun {
        val cropBundle = cropBundles[cropBundlesPosition]

        val addedScreenshotDeletionInquiryUri = addScreenshotDeletionInquiryUri(
            deleteScreenshot,
            context,
            cropBundle.screenshot.uri
        )

        return {
            val (savingResult, successfullyDeleted) = context.processCropBundle(
                cropBundle,
                validSaveDirDocumentUri,
                deleteScreenshot && !addedScreenshotDeletionInquiryUri
            )

            savingResult.let{ (successful, uri) ->
                if (successful)
                    savedCropUris.add(uri)
            }

            if (successfullyDeleted == true)
                nDeletedScreenshots++
        }
    }

    private fun addScreenshotDeletionInquiryUri(deleteScreenshot: Boolean,
                                                context: Context,
                                                screenshotUri: Uri): Boolean{
        if (deleteScreenshot)
            context.imageDeletionInquiryUri(screenshotUri)?.let {
                screenshotDeletionInquiryUris.add(it)
                return true
            }
        return false
    }

    fun cropWriteDirIdentifier(): String =
        validSaveDirDocumentUri?.let {
            documentUriPathIdentifier(it)
        } ?: externalPicturesDir.path
}