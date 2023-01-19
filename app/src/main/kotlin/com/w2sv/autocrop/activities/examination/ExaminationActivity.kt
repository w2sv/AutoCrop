package com.w2sv.autocrop.activities.examination

import android.content.Context
import android.net.Uri
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
import com.w2sv.autocrop.cropbundle.io.CropBundleIORunner
import com.w2sv.autocrop.cropbundle.io.getDeleteRequestUri
import com.w2sv.autocrop.preferences.BooleanPreferences
import com.w2sv.autocrop.preferences.GlobalFlags
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

        /**
         * Clear [cropBundles]
         */
        override fun onCleared() {
            super.onCleared()

            cropBundles.clear()
        }

        val accumulatedIoResults = AccumulatedIOResults()
        val deleteRequestUris = arrayListOf<Uri>()

        fun onDeleteRequestUrisDeleted(){
            accumulatedIoResults.nDeletedScreenshots += deleteRequestUris.size
        }

        fun processCropBundle(cropBundlePosition: Int, context: Context) {
            val cropBundle = cropBundles[cropBundlePosition]

            getAndAccumulateCropBundleIOResult(
                cropBundle,
                addScreenshotDeleteRequestUriIfApplicable(cropBundle.screenshot.mediaStoreData.id),
                context
            )
        }

        fun processCropBundleAsScopedCoroutine(cropBundlePosition: Int, context: Context) {
            val cropBundle = cropBundles[cropBundlePosition]
            val deleteScreenshotWODeletionRequest = addScreenshotDeleteRequestUriIfApplicable(cropBundle.screenshot.mediaStoreData.id)

            cropProcessingCoroutine = viewModelScope.launch(Dispatchers.IO) {
                getAndAccumulateCropBundleIOResult(cropBundle, deleteScreenshotWODeletionRequest, context)
            }
        }

        var cropProcessingCoroutine: Job? = null
            private set

        private fun getAndAccumulateCropBundleIOResult(
            cropBundle: CropBundle,
            deleteScreenshot: Boolean,
            context: Context
        ) {
            accumulatedIoResults.addFrom(
                CropBundleIORunner.getInstance(context).invoke(
                    cropBundle,
                    deleteScreenshot
                )
            )
        }

        /**
         * @return bool: indicating whether screenshot to be deleted w/o deletion request
         */
        private fun addScreenshotDeleteRequestUriIfApplicable(
            screenshotMediaStoreId: Long
        ): Boolean =
            when (booleanPreferences.deleteScreenshots) {
                false -> false
                else -> getDeleteRequestUri(screenshotMediaStoreId)?.let {
                    deleteRequestUris.add(it)
                    false
                }
                    ?: true
            }
    }

    override fun getRootFragment(): Fragment =
        CropPagerFragment.getInstance(
            intent.getParcelable(CropResults.EXTRA)!!,
        )

    private val viewModel: ViewModel by viewModels()

    @Inject
    lateinit var globalFlags: GlobalFlags

    @Inject
    lateinit var booleanPreferences: BooleanPreferences

    override val lifecycleObservers: List<LifecycleObserver>
        get() = listOf(globalFlags, booleanPreferences)

    /**
     * Invoke [DeleteRequestFragment] if there are screenshots whose
     * deletion has to be confirmed, otherwise [AppTitleFragment]
     */
    fun replaceWithSubsequentFragment() {
        fragmentReplacementTransaction(
            if (viewModel.deleteRequestUris.isNotEmpty())
                DeleteRequestFragment()
            else
                AppTitleFragment(),
            true
        )
            .commit()
    }

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

    fun startMainActivity() {
        MainActivity.start(this) {
            putExtra(AccumulatedIOResults.EXTRA, viewModel.accumulatedIoResults)
        }
    }
}