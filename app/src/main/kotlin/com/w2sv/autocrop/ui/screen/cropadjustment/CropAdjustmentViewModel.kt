package com.w2sv.autocrop.ui.screen.cropadjustment

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.RectF
import androidx.core.util.lruCache
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.w2sv.autocrop.ui.screen.cropadjustment.extensions.asMappedFrom
import com.w2sv.autocrop.ui.screen.cropadjustment.extensions.asRectF
import com.w2sv.autocrop.ui.screen.cropadjustment.extensions.getRectF
import com.w2sv.autocrop.ui.screen.cropadjustment.model.EdgeSelectionState
import com.w2sv.autocrop.ui.screen.cropadjustment.model.Line
import com.w2sv.cropbundle.CropBundle
import com.w2sv.cropbundle.cropping.model.CropEdges
import com.w2sv.domain.model.CropAdjustmentMode
import com.w2sv.domain.repository.PreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val N_SCREEN_ORIENTATIONS: Int = 2

@HiltViewModel
class CropAdjustmentViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    contentResolver: ContentResolver,
    private val preferencesRepository: PreferencesRepository
) : androidx.lifecycle.ViewModel() {

    private val cropBundle: CropBundle =
        ExaminationActivity.ViewModel.cropBundles[savedStateHandle[CropBundle.EXTRA_POSITION]!!]
    val screenshotBitmap: Bitmap = cropBundle.screenshot.getBitmap(contentResolver)

    /**
     * CropAdjustmentView dependencies
     */

    private val edgeCandidatePoints: FloatArray by lazy {
        cropBundle.edgeCandidates.map {
            listOf(
                0f,
                it.toFloat(),
                screenshotBitmap.width.toFloat(),
                it.toFloat()
            )
        }
            .flatten()
            .toFloatArray()
    }

    val initialCropRectF: RectF by lazy {
        cropBundle.crop.edges.asRectF(screenshotBitmap.width)
    }

    val imageRect: RectF by lazy {
        screenshotBitmap.getRectF()
    }

    val edgeCandidateLinesViewDomainCache = lruCache<Matrix, List<Line>>(
        maxSize = N_SCREEN_ORIENTATIONS,
        create = { matrix ->
            FloatArray(edgeCandidatePoints.size)
                .asMappedFrom(edgeCandidatePoints, matrix)
                .toList()
                .windowed(4, 4)
        }
    )

    val edgeCandidateYsViewDomainCache =
        lruCache<Matrix, List<Float>>(
            N_SCREEN_ORIENTATIONS,
            create = { matrix -> edgeCandidateLinesViewDomainCache.get(matrix).map { it[1] } }
        )

    /**
     * CropAdjustmentMode
     */

    val adjustmentMode = preferencesRepository.cropAdjustmentMode.stateIn(viewModelScope, SharingStarted.Eagerly)

    fun saveAdjustmentMode(value: CropAdjustmentMode) {
        viewModelScope.launch { preferencesRepository.cropAdjustmentMode.save(value) }
    }

    /**
     * CropEdges
     */

    val cropEdges: LiveData<CropEdges?> get() = _cropEdges
    private val _cropEdges = MutableLiveData(cropBundle.crop.edges)

    fun postCropEdges(value: CropEdges?) {
        _cropEdgesHaveChanged.postValue(value != null && value != _cropEdges.value)
        _cropEdges.postValue(value)
    }

    val cropEdgesHaveChanged: LiveData<Boolean> get() = _cropEdgesHaveChanged
    private val _cropEdgesHaveChanged = MutableLiveData(false)

    fun resetCropEdges() {
        _cropEdges.postValue(cropBundle.crop.edges)
    }

    /**
     * Selected Edges
     */

    val edgeSelectionState: LiveData<EdgeSelectionState> get() = _edgeSelectionState
    private val _edgeSelectionState = MutableLiveData<EdgeSelectionState>(EdgeSelectionState.Unselected)

    fun postEdgeSelectionState(value: EdgeSelectionState) {
        _edgeSelectionState.postValue(value)
    }
}