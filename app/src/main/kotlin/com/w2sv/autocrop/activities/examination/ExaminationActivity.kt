package com.w2sv.autocrop.activities.examination

import android.content.Context
import android.net.Uri
import androidx.activity.viewModels
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.viewModelScope
import com.w2sv.androidutils.extensions.showToast
import com.w2sv.autocrop.activities.AppActivity
import com.w2sv.autocrop.activities.crop.domain.CropResults
import com.w2sv.autocrop.activities.examination.fragments.adjustment.CropAdjustmentFragment
import com.w2sv.autocrop.activities.examination.fragments.comparison.ComparisonFragment
import com.w2sv.autocrop.activities.examination.fragments.exit.ExitFragment
import com.w2sv.autocrop.activities.examination.fragments.pager.CropPagerFragment
import com.w2sv.autocrop.activities.examination.fragments.saveall.SaveAllFragment
import com.w2sv.autocrop.activities.main.MainActivity
import com.w2sv.autocrop.domain.AccumulatedIOResults
import com.w2sv.common.extensions.getParcelableExtraCompat
import com.w2sv.cropbundle.CropBundle
import com.w2sv.cropbundle.io.CropBundleIORunner
import com.w2sv.cropbundle.io.getDeleteRequestUri
import com.w2sv.common.preferences.BooleanPreferences
import com.w2sv.common.preferences.EnumOrdinals
import com.w2sv.common.preferences.GlobalFlags
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
        private val booleanPreferences: BooleanPreferences,
        globalFlags: GlobalFlags,
        enumOrdinals: EnumOrdinals
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

        val lifecycleObservers: List<LifecycleObserver> = listOf(booleanPreferences, globalFlags, enumOrdinals)

        val accumulatedIoResults = AccumulatedIOResults()
        val deleteRequestUris = arrayListOf<Uri>()

        val deleteRequestUrisPresent: Boolean get() = deleteRequestUris.isNotEmpty()

        fun onDeleteRequestUrisDeleted() {
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
            intent.getParcelableExtraCompat(CropResults.EXTRA)!!
        )

    private val viewModel: ViewModel by viewModels()

    override val lifecycleObservers: List<LifecycleObserver> get() = viewModel.lifecycleObservers

    fun invokeExitFragment() {
        fragmentReplacementTransaction(
            ExitFragment(),
            true
        )
            .commit()
    }

    override fun handleOnBackPressed() {
        getCurrentFragment().let {
            when (it) {
                is ComparisonFragment -> it.popFromFragmentManager(supportFragmentManager)
                is CropAdjustmentFragment -> supportFragmentManager.popBackStack()
                is SaveAllFragment -> showToast("Wait until crops have been saved")
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