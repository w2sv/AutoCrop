package com.autocrop.activities.iodetermination

import android.content.ContentResolver
import android.net.Uri
import android.provider.MediaStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.autocrop.CropBundle
import com.autocrop.Screenshot
import com.autocrop.utils.android.extensions.queryMediaStoreDatum
import com.autocrop.utils.kotlin.BlankFun
import com.autocrop.utils.kotlin.delegates.AutoSwitch
import com.autocrop.utils.kotlin.extensions.toInt
import kotlinx.coroutines.Job

class IODeterminationActivityViewModel(
    private val validSaveDirDocumentUri: Uri?,
    val nDismissedScreenshots: Int
) : ViewModel() {

    class Factory(
        private val validSaveDirDocumentUri: Uri?,
        private val nDismissedScreenshots: Int
    ) : ViewModelProvider.NewInstanceFactory() {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            IODeterminationActivityViewModel(
                validSaveDirDocumentUri,
                nDismissedScreenshots
            ) as T
    }

    companion object {
        lateinit var cropBundles: MutableList<CropBundle>
    }

    /**
     * Clear [cropBundles]
     */
    override fun onCleared() {
        super.onCleared()

        cropBundles.clear()
    }

    var showedDismissedScreenshotsSnackbar by AutoSwitch(false, switchOn = false)

    var singularCropSavingJob: Job? = null

    var nDeletedScreenshots = 0
    val writeUris = arrayListOf<Uri>()
    val deletionInquiryUris = arrayListOf<Uri>()

    fun makeCropBundleProcessor(
        cropBundlePosition: Int,
        deleteScreenshot: Boolean,
        contentResolver: ContentResolver
    ): BlankFun {
        val cropBundle = cropBundles[cropBundlePosition]

        val addedScreenshotDeletionInquiryUri = addScreenshotDeleteRequestUri(
            deleteScreenshot,
            cropBundle.screenshot
        )

        return {
            val ioResult = contentResolver.carryOutCropIO(
                cropBundle.crop.bitmap,
                cropBundle.screenshot.mediaStoreData,
                validSaveDirDocumentUri,
                deleteScreenshot && !addedScreenshotDeletionInquiryUri
            )

            if (ioResult.successfullySavedCrop)
                writeUris.add(ioResult.writeUri!!)
            nDeletedScreenshots += (ioResult.deletedScreenshot == true).toInt()
        }
    }

    private fun addScreenshotDeleteRequestUri(
        deleteScreenshot: Boolean,
        screenshot: Screenshot
    ): Boolean {
        if (deleteScreenshot)
            deleteRequestUri(screenshot.mediaStoreData.id)?.let {
                deletionInquiryUris.add(it)
                return true
            }
        return false
    }

    fun cropWriteDirIdentifier(contentResolver: ContentResolver): String? =
        if (writeUris.isEmpty())
            null
        else
            contentResolver.queryMediaStoreDatum(
                writeUris.first(),
                MediaStore.Images.Media.DATA
            )
                .split("/")
                .run {
                    "/${get(lastIndex - 1)}"
                }
}