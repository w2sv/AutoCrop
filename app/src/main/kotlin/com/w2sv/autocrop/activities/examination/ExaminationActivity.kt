package com.w2sv.autocrop.activities.examination

import android.content.Context
import android.net.Uri
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.viewModelScope
import com.w2sv.autocrop.R
import com.w2sv.autocrop.activities.ApplicationActivity
import com.w2sv.autocrop.activities.crop.CropResults
import com.w2sv.autocrop.activities.examination.fragments.apptitle.AppTitleFragment
import com.w2sv.autocrop.activities.examination.fragments.comparison.ComparisonFragment
import com.w2sv.autocrop.activities.examination.fragments.croppager.CropPagerFragment
import com.w2sv.autocrop.activities.examination.fragments.deletionconfirmation.DeleteRequestFragment
import com.w2sv.autocrop.activities.examination.fragments.manualcrop.ManualCropFragment
import com.w2sv.autocrop.activities.examination.fragments.saveall.SaveAllFragment
import com.w2sv.autocrop.activities.main.MainActivity
import com.w2sv.autocrop.cropbundle.CropBundle
import com.w2sv.autocrop.cropbundle.Screenshot
import com.w2sv.autocrop.cropbundle.io.CropBundleIORunner
import com.w2sv.autocrop.cropbundle.io.getDeleteRequestUri
import com.w2sv.autocrop.preferences.BooleanPreferences
import com.w2sv.autocrop.preferences.Flags
import com.w2sv.autocrop.utils.extensions.getParcelable
import com.w2sv.autocrop.utils.extensions.snackyBuilder
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ExaminationActivity : ApplicationActivity() {

    @HiltViewModel
    class ViewModel @Inject constructor(
        private val booleanPreferences: BooleanPreferences
    ) : androidx.lifecycle.ViewModel() {

        companion object {
            lateinit var cropBundles: MutableList<CropBundle>
        }

        val ioResults = IOResults()

        val deletionInquiryUris = arrayListOf<Uri>()

        fun makeCropIOProcessor(cropBundlePosition: Int, applicationContext: Context): () -> Unit {
            val cropBundle = cropBundles[cropBundlePosition]
            val deleteScreenshot = booleanPreferences.deleteScreenshots

            val addedScreenshotDeletionInquiryUri = addScreenshotDeleteRequestUri(
                deleteScreenshot,
                cropBundle.screenshot
            )

            return {
                val ioResult = CropBundleIORunner.getInstance(applicationContext).invoke(
                    cropBundle.crop.bitmap,
                    cropBundle.screenshot.mediaStoreData,
                    deleteScreenshot && !addedScreenshotDeletionInquiryUri
                )

                ioResults.addFrom(ioResult)
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

        /**
         * Clear [cropBundles]
         */
        override fun onCleared() {
            super.onCleared()

            cropBundles.clear()
        }
    }

    override fun getRootFragment(): Fragment =
        CropPagerFragment.getInstance(
            intent.getParcelable(CropResults.EXTRA)!!,
        )

    private val viewModel: ViewModel by viewModels()

    @Inject
    lateinit var flags: Flags
    @Inject
    lateinit var booleanPreferences: BooleanPreferences

    override val lifecycleObservers: List<LifecycleObserver>
        get() = listOf(flags, booleanPreferences)

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
        MainActivity.start(this) {
            putExtra(IOResults.EXTRA, viewModel.ioResults)
        }
    }
}