package com.w2sv.cropping.session

import android.content.Context
import android.net.Uri
import com.w2sv.cropping.io.CropBundleIOProcessingUseCase
import com.w2sv.domain.model.CropBundle
import com.w2sv.domain.model.CropBundleProcessingResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.coroutineContext

class CropSession(private val ioProcessingUseCase: CropBundleIOProcessingUseCase) {

    private val _bundles = mutableListOf<CropBundle>()
    val bundles: List<CropBundle> get() = _bundles

    fun add(bundle: CropBundle) {
        _bundles.add(bundle)
    }

    fun removeBundleAt(index: Int) {
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

    suspend fun processCropBundleAt(index: Int, context: Context, onFinished: suspend () -> Unit = {}) {
        processBundle(
            bundle = bundles[index],
            context = context,
            onFinished = {
                _bundles.removeAt(index)
                onFinished()
            }
        )
    }

    suspend fun processAllBundles(context: Context, onBundleProcessed: suspend () -> Unit = {}) {
        val iterator = _bundles.iterator()
        while (iterator.hasNext()) {
            coroutineContext.ensureActive()

            val bundle = iterator.next()
            processBundle(
                bundle = bundle,
                context = context,
                onFinished = {
                    iterator.remove()
                    onBundleProcessed()
                }
            )
        }
    }

    private suspend fun processBundle(bundle: CropBundle, context: Context, onFinished: suspend () -> Unit) {
        coroutineScope {
            cropProcessingJob = launch(Dispatchers.IO) {
                val processingResult = ioProcessingUseCase.invoke(
                    cropBundle = bundle,
                    context = context
                )
                withContext(NonCancellable) {
                    _cropBundleProcessingResults.add(processingResult)
                    onFinished()
                }
            }
        }
    }

    var cropProcessingJob: Job? = null
        private set
}
