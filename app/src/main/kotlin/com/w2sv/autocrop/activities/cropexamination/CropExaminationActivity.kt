package com.w2sv.autocrop.activities.cropexamination

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.fragment.app.Fragment
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.w2sv.autocrop.R
import com.w2sv.autocrop.activities.ApplicationActivity
import com.w2sv.autocrop.activities.crop.CropActivity
import com.w2sv.autocrop.activities.cropexamination.fragments.apptitle.AppTitleFragment
import com.w2sv.autocrop.activities.cropexamination.fragments.comparison.ComparisonFragment
import com.w2sv.autocrop.activities.cropexamination.fragments.croppager.CropPagerFragment
import com.w2sv.autocrop.activities.cropexamination.fragments.deletionconfirmation.DeleteRequestFragment
import com.w2sv.autocrop.activities.cropexamination.fragments.manualcrop.ManualCropFragment
import com.w2sv.autocrop.activities.cropexamination.fragments.saveall.SaveAllFragment
import com.w2sv.autocrop.activities.main.MainActivity
import com.w2sv.autocrop.cropbundle.CropBundle
import com.w2sv.autocrop.cropbundle.Screenshot
import com.w2sv.autocrop.cropbundle.io.CropBundleIOProcessor
import com.w2sv.autocrop.cropbundle.io.extensions.queryMediaStoreDatum
import com.w2sv.autocrop.cropbundle.io.getDeleteRequestUri
import com.w2sv.autocrop.preferences.BooleanPreferences
import com.w2sv.autocrop.utils.extensions.getInt
import com.w2sv.autocrop.utils.extensions.snackyBuilder
import com.w2sv.kotlinutils.extensions.toInt
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class CropExaminationActivity : ApplicationActivity() {

    private companion object {
        const val EXTRA_CROP_URIS = "com.w2sv.autocrop.CROP_URIS"
        const val EXTRA_N_DELETED_SCREENSHOTS = "com.w2sv.autocrop.N_DELETED_SCREENSHOTS"
        const val EXTRA_SAVE_DIR_NAME = "com.w2sv.autocrop.SAVE_DIR_NAME"
    }

    override fun getRootFragment(): Fragment =
        CropPagerFragment.getInstance(
            intent.getInt(CropActivity.EXTRA_N_UNCROPPED_SCREENSHOTS)
        )

    @HiltViewModel
    class ViewModel @Inject constructor(
        private val booleanPreferences: BooleanPreferences
    ) : androidx.lifecycle.ViewModel() {

        companion object {
            lateinit var cropBundles: MutableList<CropBundle>
        }

        var nDeletedScreenshots = 0
        val writeUris = arrayListOf<Uri>()
        val deletionInquiryUris = arrayListOf<Uri>()

        fun makeCropIOProcessor(cropBundlePosition: Int, applicationContext: Context): () -> Unit {
            val cropBundle = cropBundles[cropBundlePosition]
            val deleteScreenshot = booleanPreferences.deleteScreenshots

            val addedScreenshotDeletionInquiryUri = addScreenshotDeleteRequestUri(
                deleteScreenshot,
                cropBundle.screenshot
            )

            return {
                val ioResult = CropBundleIOProcessor.getInstance(applicationContext).invoke(
                    cropBundle.crop.bitmap,
                    cropBundle.screenshot.mediaStoreData,
                    deleteScreenshot && !addedScreenshotDeletionInquiryUri
                )

                if (ioResult.successfullySavedCrop)
                    writeUris.add(ioResult.cropWriteUri!!)

                nDeletedScreenshots += (ioResult.deletedScreenshot == true).toInt()
            }
        }

        var cropProcessingCoroutine: Job? = null

        fun launchViewModelScopedCropProcessingCoroutine(cropBundlePosition: Int, applicationContext: Context) {
            val processCropBundle = makeCropIOProcessor(cropBundlePosition, applicationContext)

            cropProcessingCoroutine = viewModelScope.launch(Dispatchers.IO) {
                processCropBundle()
            }
        }

        private fun addScreenshotDeleteRequestUri(
            deleteScreenshot: Boolean,
            screenshot: Screenshot
        ): Boolean {
            if (deleteScreenshot)
                getDeleteRequestUri(screenshot.mediaStoreData.id)?.let {
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

    private val viewModel: ViewModel by viewModels()

    /**
     * Invoke [DeleteRequestFragment] if there are screenshots whose
     * deletion has to be confirmed, otherwise [AppTitleFragment]
     */
    fun replaceWithSubsequentFragment() {
        fragmentReplacementTransaction(
            if (viewModel.deletionInquiryUris.isNotEmpty())
                DeleteRequestFragment()
            else
                AppTitleFragment(),
            true
        )
            .commit()
    }

    override val onBackPressedCallback: OnBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            getCurrentFragment().let {
                when (it) {
                    is ComparisonFragment -> it.popFromFragmentManager(supportFragmentManager)
                    is ManualCropFragment -> supportFragmentManager.popBackStack()
                    is SaveAllFragment -> {
                        snackyBuilder("Wait until crops have been saved")
                            .setIcon(R.drawable.ic_front_hand_24)
                            .build()
                            .show()
                    }

                    is CropPagerFragment -> it.onBackPress()
                    else -> Unit
                }
            }
        }
    }

    fun startMainActivity() {
        MainActivity.restart(this) {
            putParcelableArrayListExtra(EXTRA_CROP_URIS, viewModel.writeUris)
            putExtra(EXTRA_N_DELETED_SCREENSHOTS, viewModel.nDeletedScreenshots)
            putExtra(EXTRA_SAVE_DIR_NAME, viewModel.cropWriteDirIdentifier(contentResolver))
        }
    }

    data class Results(val cropUris: ArrayList<Uri>, val nDeletedScreenshots: Int, val saveDirName: String?) {
        companion object {
            fun restore(savedStateHandle: SavedStateHandle): Results? =
                savedStateHandle.run {
                    get<ArrayList<Uri>>(EXTRA_CROP_URIS)?.let {
                        Results(
                            it,
                            get(EXTRA_N_DELETED_SCREENSHOTS)!!,
                            get(EXTRA_SAVE_DIR_NAME)
                        )
                    }
                }
        }

        val nSavedCrops: Int get() = cropUris.size
    }
}