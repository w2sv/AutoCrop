package com.w2sv.autocrop.ui.screen

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.w2sv.androidutils.lifecycle.increment
import com.w2sv.autocrop.R
import com.w2sv.autocrop.ui.util.nonNullValue
import com.w2sv.cropbundle.CropBundle
import com.w2sv.cropbundle.io.CropBundleIOProcessingUseCase
import com.w2sv.cropbundle.io.CropBundleIOResult
import com.w2sv.cropbundle.io.ScreenshotDeletionResult
import com.w2sv.domain.repository.PreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

inline fun <reified VM : ViewModel> Fragment.cropNavGraphViewModel(): Lazy<VM> =
    hiltNavGraphViewModels<VM>(R.id.crop_nav_graph)

@HiltViewModel
class CropBundleViewModel @Inject constructor(
    private val cropBundleIOProcessingUseCase: CropBundleIOProcessingUseCase,
    preferencesRepository: PreferencesRepository
) : ViewModel() {

    val cropBundles: List<CropBundle> get() = _cropBundles
    private val _cropBundles: MutableList<CropBundle> = mutableListOf()

    fun addCropBundle(bundle: CropBundle) {
        _cropBundles.add(bundle)
    }

    val cropBundleCount: Int get() = cropBundles.size

    private val cropBundleIOResults = mutableListOf<CropBundleIOResult>()

    fun deletionApprovalRequiringCropBundleIOResults(): List<CropBundleIOResult> =
        cropBundleIOResults.filter {
            it.screenshotDeletionResult is ScreenshotDeletionResult.DeletionApprovalRequired
        }

    fun processCropBundle(cropBundlePosition: Int, context: Context) {
        processCropBundle(cropBundles[cropBundlePosition], context)
    }

    fun processCropBundle(cropBundle: CropBundle, context: Context) {
        cropProcessingJob = viewModelScope.launch(Dispatchers.IO) {
            cropBundleIOResults.add(
                cropBundleIOProcessingUseCase.invoke(
                    cropBitmap = cropBundle.crop.bitmap,
                    screenshotMediaStoreData = cropBundle.screenshot.mediaStoreData,
                    deleteScreenshot = deleteScreenshots.value,
                    context = context
                )
            )
        }
    }

    var cropProcessingJob: Job? = null
        private set

    private val deleteScreenshots = preferencesRepository.deleteScreenshots.stateIn(
        viewModelScope,
        SharingStarted.Eagerly
    )

    val nUnprocessedCrops: Int = cropBundles.size

    val saveAllProgress: LiveData<Int> get() = _saveAllProgress
    private val _saveAllProgress = MutableLiveData(0)

    private val unprocessedCropBundles: List<CropBundle>
        get() =
            cropBundles.run {
                subList(saveAllProgress.nonNullValue, size)
            }

    suspend fun saveAllCoroutine(context: Context, onFinishedListener: () -> Unit) {
        coroutineScope {
            unprocessedCropBundles.forEach { cropBundle ->
                withContext(Dispatchers.IO) {
                    processCropBundle(
                        cropBundle,
                        context
                    )
                }
                withContext(Dispatchers.Main) {
                    _saveAllProgress.increment()
                }
            }

            onFinishedListener()
        }
    }
}