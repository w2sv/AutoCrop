package com.w2sv.autocrop.activities.iodetermination

import android.content.ContentResolver
import android.net.Uri
import android.provider.MediaStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.w2sv.autocrop.CropBundle
import com.w2sv.autocrop.Screenshot
import com.w2sv.autocrop.utils.UnitFun
import com.w2sv.autocrop.utils.android.extensions.queryMediaStoreDatum
import com.w2sv.kotlinutils.delegates.AutoSwitch
import com.w2sv.kotlinutils.extensions.toInt
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
    ): UnitFun {
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