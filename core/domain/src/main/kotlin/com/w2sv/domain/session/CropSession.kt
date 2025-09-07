package com.w2sv.domain.session

import android.net.Uri
import com.w2sv.domain.model.CropBundle
import com.w2sv.domain.model.CropBundleProcessingResult

class CropSession {

    private val _bundles = mutableListOf<CropBundle>()
    val bundles: List<CropBundle> get() = _bundles

    fun add(bundle: CropBundle) {
        _bundles.add(bundle)
    }

    fun removeAt(index: Int) {
        _bundles.removeAt(index)
    }

    fun update(index: Int, newBundle: CropBundle) {
        _bundles[index] = newBundle
    }

    private var _uncroppableImageUris = mutableListOf<Uri>()
    val uncroppableImageUris: List<Uri> get() = _uncroppableImageUris

    fun addUncroppableImage(uri: Uri) {
        _uncroppableImageUris.add(uri)
    }

    private var _unopenableImageUris = mutableListOf<Uri>()
    val unopenableImageUris: List<Uri> get() = _unopenableImageUris

    fun addUnopenableImage(uri: Uri) {
        _uncroppableImageUris.add(uri)
    }

    private val _cropBundleProcessingResults = mutableListOf<CropBundleProcessingResult>()
    val cropBundleProcessingResults: List<CropBundleProcessingResult> get() = _cropBundleProcessingResults

    fun addCropBundleProcessingResult(results: CropBundleProcessingResult) {
        _cropBundleProcessingResults.add(results)
    }
}
