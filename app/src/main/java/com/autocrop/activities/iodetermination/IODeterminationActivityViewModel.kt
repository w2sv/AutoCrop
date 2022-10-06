package com.autocrop.activities.iodetermination

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.lifecycle.ViewModel
import com.autocrop.dataclasses.CropBundle
import com.autocrop.dataclasses.Screenshot
import com.autocrop.utils.android.documentUriPathIdentifier
import com.autocrop.utils.android.externalPicturesDir
import com.autocrop.utils.kotlin.BlankFun
import com.autocrop.utils.kotlin.delegates.AutoSwitch
import kotlinx.coroutines.Job
import timber.log.Timber

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

        val addedScreenshotDeletionInquiryUri = addScreenshotDeletionInquiryUri(
            deleteScreenshot,
            cropBundle.screenshot
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
                                                screenshot: Screenshot): Boolean{
        if (deleteScreenshot)
            imageDeletionInquiryUri(screenshot.mediaStoreColumns.id)?.let {
                screenshotDeletionInquiryUris.add(it)
                return true
            }
        return false
    }

    private fun imageDeletionInquiryUri(mediaStoreId: Long): Uri? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
            ContentUris.withAppendedId(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                mediaStoreId
            )
                .also { Timber.i("Built contentUriWithMediaStoreImagesId: $it") }
        else
            null

    fun cropWriteDirIdentifier(): String =
        validSaveDirDocumentUri?.let {
            documentUriPathIdentifier(it)
        } ?: externalPicturesDir.path
}