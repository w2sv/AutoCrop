package com.w2sv.autocrop.ui.screen

import android.content.Context
import androidx.annotation.MainThread
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.w2sv.androidutils.lifecycle.increment
import com.w2sv.autocrop.R
import com.w2sv.autocrop.ui.util.nonNullValue
import com.w2sv.cropping.io.CropBundleIOProcessingUseCase
import com.w2sv.domain.model.CropBundle
import com.w2sv.domain.model.CropBundleProcessingResult
import com.w2sv.domain.model.Screenshot
import com.w2sv.domain.repository.PreferencesRepository
import com.w2sv.domain.session.CropSession
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.lifecycle.withCreationCallback
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

inline fun <reified VM : ViewModel> Fragment.cropNavGraphViewModel(): Lazy<VM> =
    hiltNavGraphViewModels<VM>(R.id.crop_nav_graph)

@HiltViewModel
class CropSessionViewModel @Inject constructor(val cropSession: CropSession) : ViewModel()

interface CropSessionAccessingViewModelFactory<VM : ViewModel> {
    fun create(cropSession: CropSession): VM
}

@MainThread
inline fun <reified VM : ViewModel, F : CropSessionAccessingViewModelFactory<VM>> Fragment.cropSessionInjectedViewModel(): Lazy<VM> {
    val cropSessionViewModel by cropNavGraphViewModel<CropSessionViewModel>()
    return viewModels<VM>(
        extrasProducer = {
            defaultViewModelCreationExtras
                .withCreationCallback<F> { factory ->
                    factory.create(cropSessionViewModel.cropSession)
                }
        }
    )
}

@HiltViewModel
class CropBundleViewModel @Inject constructor(
    private val cropBundleIOProcessingUseCase: CropBundleIOProcessingUseCase,
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {

    val cropBundles: List<CropBundle> get() = _cropBundles
    private val _cropBundles: MutableList<CropBundle> = mutableListOf()

    fun addCropBundle(bundle: CropBundle) {
        _cropBundles.add(bundle)
    }

    fun discardCropBundleAt(index: Int) {
        _cropBundles.removeAt(index)
    }

    val deleteScreenshots = preferencesRepository.deleteScreenshots.stateIn(viewModelScope, SharingStarted.Eagerly)

    fun toggleDeleteScreenshots() {
        viewModelScope.launch { preferencesRepository.deleteScreenshots.save(!deleteScreenshots.value) }
    }

    val cropBundleCount: Int get() = cropBundles.size

    private val cropBundleIOResults = mutableListOf<CropBundleProcessingResult>()

    fun deletionApprovalRequiringCropBundleIOResults(): List<CropBundleProcessingResult> =
        cropBundleIOResults.filter {
            it.screenshotDeletionResult is Screenshot.DeletionResult.ApprovalRequired
        }

    fun processCropBundleAt(index: Int, context: Context) {
        processCropBundle(cropBundles[index], context)
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

    val nUnprocessedCrops: Int = cropBundles.size

    val saveAllProgress: LiveData<Int> get() = _saveAllProgress
    private val _saveAllProgress = MutableLiveData(0)

    private val unprocessedCropBundles: List<CropBundle>
        get() = cropBundles.run {
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
