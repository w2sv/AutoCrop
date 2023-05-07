package com.w2sv.autocrop.activities.examination

import android.app.Activity
import android.content.Context
import androidx.fragment.app.Fragment
import androidx.lifecycle.viewModelScope
import com.w2sv.androidutils.generic.getParcelableCompat
import com.w2sv.androidutils.notifying.showToast
import com.w2sv.autocrop.R
import com.w2sv.autocrop.activities.AppActivity
import com.w2sv.autocrop.activities.crop.domain.CropResults
import com.w2sv.autocrop.activities.examination.fragments.adjustment.CropAdjustmentFragment
import com.w2sv.autocrop.activities.examination.fragments.comparison.ComparisonFragment
import com.w2sv.autocrop.activities.examination.fragments.exit.ExitFragment
import com.w2sv.autocrop.activities.examination.fragments.pager.CropPagerFragment
import com.w2sv.autocrop.activities.examination.fragments.saveall.SaveAllFragment
import com.w2sv.autocrop.domain.AccumulatedIOResults
import com.w2sv.autocrop.utils.extensions.startMainActivity
import com.w2sv.cropbundle.CropBundle
import com.w2sv.cropbundle.io.CropBundleIOResult
import com.w2sv.cropbundle.io.CropBundleIORunner
import com.w2sv.cropbundle.io.ScreenshotDeletionResult
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ExaminationActivity : AppActivity() {

    @HiltViewModel
    class ViewModel @Inject constructor(
        @ApplicationContext context: Context
    ) : androidx.lifecycle.ViewModel() {

        val cropBundleIOResults = mutableListOf<CropBundleIOResult>()

        val deleteApprovalRequiringCropBundleIOResults by lazy {  // TODO: Move to ExitFragment ViewModel
            cropBundleIOResults.filter {
                it.screenshotDeletionResult is ScreenshotDeletionResult.SuccessfullyDeleted
            }
        }

        fun processCropBundleAsScopedCoroutine(cropBundlePosition: Int, context: Context) {
            cropProcessingCoroutine = viewModelScope.launch(Dispatchers.IO) {
                addCropBundleIOResult(cropBundlePosition, context)
            }
        }

        var cropProcessingCoroutine: Job? = null
            private set

        fun addCropBundleIOResult(
            cropBundlePosition: Int,
            context: Context
        ) {
            val cropBundle = cropBundles[cropBundlePosition]

            cropBundleIOResults.add(
                cropBundleIORunner.invoke(
                    cropBundle.crop.bitmap,
                    cropBundle.screenshot.mediaStoreData,
                    context
                )
            )
        }

        private val cropBundleIORunner = CropBundleIORunner.getInstance(context)

        fun startMainActivity(activity: Activity) {
            activity.startMainActivity {
                putExtra(AccumulatedIOResults.EXTRA, AccumulatedIOResults.get(cropBundleIOResults))
            }
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
        fragmentReplacementTransaction(
            ExitFragment(),
            true
        )
            .commit()
    }

    override fun handleOnBackPressed() {
        when (val fragment = getCurrentFragment()) {
            is ComparisonFragment -> fragment.popFromFragmentManager(supportFragmentManager)
            is CropAdjustmentFragment -> supportFragmentManager.popBackStack()
            is SaveAllFragment -> showToast(getString(R.string.wait_until_crops_have_been_saved))
            is CropPagerFragment -> fragment.onBackPress()
            else -> Unit
        }
    }
}