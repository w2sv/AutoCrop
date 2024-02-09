package com.w2sv.autocrop.activities.examination

import android.content.Context
import androidx.activity.viewModels
import androidx.fragment.app.Fragment
import androidx.lifecycle.viewModelScope
import com.w2sv.androidutils.coroutines.invokeOnCompletion
import com.w2sv.androidutils.generic.getParcelableCompat
import com.w2sv.androidutils.notifying.showToast
import com.w2sv.autocrop.R
import com.w2sv.autocrop.activities.AppActivity
import com.w2sv.autocrop.activities.crop.domain.CropResults
import com.w2sv.autocrop.activities.examination.adjustment.CropAdjustmentFragment
import com.w2sv.autocrop.activities.examination.comparison.ComparisonFragment
import com.w2sv.autocrop.activities.examination.exit.ExitFragment
import com.w2sv.autocrop.activities.examination.pager.CropPagerFragment
import com.w2sv.autocrop.activities.examination.saveall.SaveAllFragment
import com.w2sv.autocrop.activities.main.MainActivity
import com.w2sv.cropbundle.CropBundle
import com.w2sv.cropbundle.io.CropBundleIOProcessingUseCase
import com.w2sv.cropbundle.io.CropBundleIOResult
import com.w2sv.cropbundle.io.ScreenshotDeletionResult
import com.w2sv.domain.repository.PreferencesRepository
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ExaminationActivity : AppActivity() {

    @HiltViewModel
    class ViewModel @Inject constructor(
        private val cropBundleIOProcessingUseCase: CropBundleIOProcessingUseCase,
        preferencesRepository: PreferencesRepository
    ) : androidx.lifecycle.ViewModel() {

        private val cropBundleIOResults = mutableListOf<CropBundleIOResult>()

        fun getDeletionApprovalRequiringCropBundleIOResults(): List<CropBundleIOResult> =
            cropBundleIOResults.filter {
                it.screenshotDeletionResult is ScreenshotDeletionResult.DeletionApprovalRequired
            }

        fun processCropBundle(cropBundlePosition: Int, context: Context) {
            cropProcessingJob = viewModelScope.launch(Dispatchers.IO) {
                addCropBundleIOResult(cropBundlePosition, context)
            }
        }

        var cropProcessingJob: Job? = null
            private set

        fun addCropBundleIOResult(
            cropBundlePosition: Int,
            context: Context
        ) {
            val cropBundle = cropBundles[cropBundlePosition]

            cropBundleIOResults.add(
                cropBundleIOProcessingUseCase.invoke(
                    cropBitmap = cropBundle.crop.bitmap,
                    screenshotMediaStoreData = cropBundle.screenshot.mediaStoreData,
                    deleteScreenshot = deleteScreenshots.value,
                    context = context
                )
            )
        }

        private val deleteScreenshots = preferencesRepository.deleteScreenshots.stateIn(
            viewModelScope,
            SharingStarted.Eagerly
        )

        fun startMainActivity(context: Context) {
            MainActivity.start(
                context = context,
                configureIntent = {
                    putExtra(IOResults.EXTRA, IOResults.get(cropBundleIOResults))
                }
            )
        }

        companion object {
            lateinit var cropBundles: MutableList<CropBundle>
        }

        /**
         * Clears [cropBundles].
         */
        override fun onCleared() {
            super.onCleared()

            cropBundles.clear()
        }
    }

    override fun getRootFragment(): Fragment =
        CropPagerFragment.getInstance(
            intent.getParcelableCompat(CropResults.EXTRA)!!
        )

    fun invokeExitFragment() {
        viewModels<ViewModel>().value.cropProcessingJob.invokeOnCompletion {
            fragmentReplacementTransaction(
                ExitFragment(),
                true
            )
                .commit()
        }
    }

    override fun handleOnBackPressed() {
        when (val fragment = getCurrentFragment()) {
            is ComparisonFragment -> fragment.popFromFragmentManager(supportFragmentManager)
            is CropAdjustmentFragment -> supportFragmentManager.popBackStack()
            is SaveAllFragment -> showToast(getString(R.string.wait_until_crops_have_been_saved))
            is CropPagerFragment -> fragment.onBackPress()
            else -> throw IllegalStateException("Invalid Fragment type")
        }
    }
}