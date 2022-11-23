package com.w2sv.autocrop.activities.cropexamination

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.w2sv.autocrop.activities.crop.CropActivity
import com.w2sv.autocrop.cropping.cropbundle.CropBundle
import com.w2sv.autocrop.cropping.cropbundle.Screenshot
import com.w2sv.autocrop.cropping.cropbundle.carryOutCropIO
import com.w2sv.autocrop.cropping.cropbundle.deleteRequestUri
import com.w2sv.autocrop.preferences.UriPreferences
import com.w2sv.autocrop.utils.android.extensions.queryMediaStoreDatum
import com.w2sv.kotlinutils.UnitFun
import com.w2sv.kotlinutils.delegates.AutoSwitch
import com.w2sv.kotlinutils.extensions.toInt
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import javax.inject.Inject

@HiltViewModel
class CropExaminationActivityViewModel @Inject constructor(savedStateHandle: SavedStateHandle) : ViewModel() {

    @Inject
    lateinit var uriPreferences: UriPreferences

    val nDismissedScreenshots: Int = savedStateHandle[CropActivity.EXTRA_N_UNCROPPED_IMAGES]!!

    companion object {
        lateinit var cropBundles: MutableList<CropBundle>
    }

    var showedDismissedScreenshotsSnackbar by AutoSwitch(false, switchOn = false)

    var singularCropSavingJob: Job? = null

    var nDeletedScreenshots = 0
    val writeUris = arrayListOf<Uri>()
    val deletionInquiryUris = arrayListOf<Uri>()

    fun makeCropBundleProcessor(
        cropBundlePosition: Int,
        deleteScreenshot: Boolean,
        context: Context
    ): UnitFun {
        val cropBundle = cropBundles[cropBundlePosition]

        val addedScreenshotDeletionInquiryUri = addScreenshotDeleteRequestUri(
            deleteScreenshot,
            cropBundle.screenshot
        )

        return {
            val ioResult = context.contentResolver.carryOutCropIO(
                cropBundle.crop.bitmap,
                cropBundle.screenshot.mediaStoreData,
                uriPreferences.validDocumentUriOrNull(context),
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

    /**
     * Clear [cropBundles]
     */
    override fun onCleared() {
        super.onCleared()

        cropBundles.clear()
    }
}