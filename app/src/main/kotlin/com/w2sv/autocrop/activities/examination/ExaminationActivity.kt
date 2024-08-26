package com.w2sv.autocrop.activities.examination

import android.app.Activity
import android.content.Context
import androidx.activity.viewModels
import androidx.fragment.app.Fragment
import androidx.lifecycle.viewModelScope
import com.w2sv.androidutils.os.getParcelableCompat
import com.w2sv.autocrop.activities.ViewBoundFragmentActivity
import com.w2sv.autocrop.activities.crop.domain.CropResults
import com.w2sv.autocrop.activities.examination.exit.ExitFragment
import com.w2sv.autocrop.activities.examination.pager.CropPagerFragment
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
import slimber.log.i
import javax.inject.Inject

@AndroidEntryPoint
class ExaminationActivity : ViewBoundFragmentActivity() {

    @HiltViewModel
    class ViewModel @Inject constructor(
        private val cropBundleIOProcessingUseCase: CropBundleIOProcessingUseCase,
        private val preferencesRepository: PreferencesRepository
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
                    screenshot = cropBundle.screenshot,
                    deleteScreenshot = deleteScreenshots.value.also { i { "Delete screnshot: $it" } },
                    context = context
                )
            )
        }

        val deleteScreenshots = preferencesRepository.deleteScreenshots.stateIn(
            viewModelScope,
            SharingStarted.Eagerly
        )

        fun toggleDeleteScreenshots() {
            viewModelScope.launch { preferencesRepository.deleteScreenshots.save(!deleteScreenshots.value) }
        }

        fun navigateToMainActivity(activity: Activity) {
            MainActivity.start(
                activity = activity,
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

    private val viewModel by viewModels<ViewModel>()

    fun invokeExitFragmentOnNoCropProcessingJobRunning() {
        viewModel.cropProcessingJob?.invokeOnCompletion {
            invokeExitFragment()
        }
            ?: invokeExitFragment()
    }

    private fun invokeExitFragment() {
        fragmentReplacementTransaction(
            ExitFragment(),
            true
        )
            .commit()
    }
}