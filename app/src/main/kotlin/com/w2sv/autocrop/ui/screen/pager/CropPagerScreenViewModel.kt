package com.w2sv.autocrop.ui.screen.pager

import android.content.Context
import android.content.res.Resources
import android.text.SpannableStringBuilder
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.core.text.bold
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.w2sv.androidutils.BackPressHandler
import com.w2sv.androidutils.widget.makeToast
import com.w2sv.androidutils.widget.showToast
import com.w2sv.autocrop.R
import com.w2sv.autocrop.ui.screen.pager.view.CropPagerWrapper
import com.w2sv.autocrop.ui.util.Constant
import com.w2sv.autocrop.ui.util.nonNullValue
import com.w2sv.cropbundle.CropBundle
import com.w2sv.domain.repository.PreferencesRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = CropPagerScreenViewModel.AssistedFactory::class)
class CropPagerScreenViewModel @AssistedInject constructor(
    savedStateHandle: SavedStateHandle,
    private val preferencesRepository: PreferencesRepository,
    private val resources: Resources,
    @Assisted cropBundles: List<CropBundle>
) : androidx.lifecycle.ViewModel() {

    @dagger.assisted.AssistedFactory
    interface AssistedFactory {
        fun create(cropBundles: List<CropBundle>): CropPagerScreenViewModel
    }

    val deleteScreenshots = preferencesRepository.deleteScreenshots.stateIn(viewModelScope, SharingStarted.Eagerly)

    fun toggleDeleteScreenshots() {
        viewModelScope.launch { preferencesRepository.deleteScreenshots.save(!deleteScreenshots.value) }
    }

    val dataSet = CropPagerWrapper.DataSet(cropBundles.toMutableList())  // TODO

    // ==================
    // AutoScroll
    // ==================

    val doAutoScroll = preferencesRepository.autoScroll.stateIn(viewModelScope, SharingStarted.Eagerly)

    fun saveDoAutoScroll(value: Boolean) {
        viewModelScope.launch {
            preferencesRepository.autoScroll.save(value)
        }
    }

    val autoScrolling: LiveData<Boolean> get() = _autoScrolling
    private val _autoScrolling = MutableLiveData(doAutoScroll.value && dataSet.size > 1)

    fun cancelAutoScroll() {
        _autoScrolling.postValue(false)
    }

    fun autoScrollCount(): Int =
        dataSet.size - dataSet.livePosition.nonNullValue

    // ==================
    // Crop Results Notification
    // ==================

    fun showCropResultsToastIfApplicable(context: Context) {
        if (uncroppedScreenshotsSnackbarText != null && !showedCropResultsNotification) {
            context.showToast(uncroppedScreenshotsSnackbarText, duration = Toast.LENGTH_LONG)
            showedCropResultsNotification = true
        }
    }

    private val uncroppedScreenshotsSnackbarText: SpannableStringBuilder? =
        SpannableStringBuilder()
            .run {
                val cropResults = CropPagerScreenFragmentArgs.fromSavedStateHandle(savedStateHandle).cropResults

                if (cropResults.uncroppableImageCount != 0) {
                    append("Couldn't find crop bounds for")
                    bold {
                        append(" ${cropResults.uncroppableImageCount}")
                    }
                    append(
                        " ${
                            resources.getQuantityString(
                                R.plurals.screenshot,
                                cropResults.uncroppableImageCount
                            )
                        }"
                    )
                }
                ifEmpty { null }
            }

    private var showedCropResultsNotification: Boolean = false

    // ==========
    // Other
    // ==========

    fun showCropProcedureResultToast(context: Context, @StringRes messageRes: Int) {
        lastCropProcedureResultToast?.cancel()
        lastCropProcedureResultToast = context
            .makeToast(messageRes)
            .also {
                it.show()
            }
    }

    private var lastCropProcedureResultToast: Toast? = null

    val backPressHandler = BackPressHandler(
        viewModelScope,
        Constant.BACKPRESS_CONFIRMATION_WINDOW_DURATION
    )
}