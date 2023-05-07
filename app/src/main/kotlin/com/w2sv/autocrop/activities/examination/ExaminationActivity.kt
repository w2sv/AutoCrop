package com.w2sv.autocrop.activities.examination

import android.app.Activity
import android.content.Context
import android.net.Uri
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
import com.w2sv.common.datastore.Repository
import com.w2sv.cropbundle.CropBundle
import com.w2sv.cropbundle.io.CropBundleIORunner
import com.w2sv.cropbundle.io.getDeleteRequestUri
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ExaminationActivity : AppActivity() {

    @HiltViewModel
    class ViewModel @Inject constructor(
        private val repository: Repository
    ) : androidx.lifecycle.ViewModel() {

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

        private val accumulatedIoResults = AccumulatedIOResults()
        val deleteRequestUris = arrayListOf<Uri>()

        fun accumulateDeleteRequestUris() {
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
            val deleteScreenshotWODeletionRequest =
                addScreenshotDeleteRequestUriIfApplicable(cropBundle.screenshot.mediaStoreData.id)

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
                CropBundleIORunner.invoke(
                    context,
                    cropBundle,
                    deleteScreenshot
                )
            )
        }

        /**
         * @return bool: whether screenshot to be deleted w/o deletion request.
         */
        private fun addScreenshotDeleteRequestUriIfApplicable(
            screenshotMediaStoreId: Long
        ): Boolean =
            when (repository.deleteScreenshots.value) {
                false -> false
                else -> getDeleteRequestUri(screenshotMediaStoreId)?.let {
                    deleteRequestUris.add(it)
                    false
                }
                    ?: true
            }

        fun startMainActivity(activity: Activity) {
            activity.startMainActivity {
                putExtra(AccumulatedIOResults.EXTRA, accumulatedIoResults)
            }
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