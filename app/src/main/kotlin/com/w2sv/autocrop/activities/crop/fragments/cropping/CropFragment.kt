package com.w2sv.autocrop.activities.crop.fragments.cropping

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import com.blogspot.atifsoftwares.animatoolib.Animatoo
import com.w2sv.androidutils.BackPressHandler
import com.w2sv.androidutils.extensions.getLong
import com.w2sv.androidutils.extensions.increment
import com.w2sv.androidutils.extensions.showToast
import com.w2sv.autocrop.R
import com.w2sv.autocrop.activities.AppFragment
import com.w2sv.autocrop.activities.crop.CropResults
import com.w2sv.autocrop.activities.crop.fragments.croppingfailed.CroppingFailedFragment
import com.w2sv.autocrop.activities.examination.ExaminationActivity
import com.w2sv.autocrop.activities.getFragment
import com.w2sv.autocrop.activities.main.MainActivity
import com.w2sv.autocrop.databinding.CropBinding
import com.w2sv.cropbundle.CropBundle
import com.w2sv.cropbundle.Screenshot
import com.w2sv.common.utils.getMediaUri
import com.w2sv.cropbundle.cropping.cropEdgesCandidates
import com.w2sv.cropbundle.cropping.maxHeightEdges
import com.w2sv.cropbundle.io.extensions.loadBitmap
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import slimber.log.i
import javax.inject.Inject

@AndroidEntryPoint
class CropFragment
    : AppFragment<CropBinding>(CropBinding::class.java) {

    companion object {
        fun getInstance(screenshotUris: List<Uri>): CropFragment =
            getFragment(CropFragment::class.java, MainActivity.EXTRA_SELECTED_IMAGE_URIS to screenshotUris)
    }

    @HiltViewModel
    class ViewModel @Inject constructor(
        savedStateHandle: SavedStateHandle,
        @ApplicationContext context: Context
    ) : androidx.lifecycle.ViewModel() {

        val backPressListener = BackPressHandler(
            viewModelScope,
            context.resources.getLong(R.integer.duration_backpress_confirmation_window)
        )

        private val screenshotUris: List<Uri> = savedStateHandle[MainActivity.EXTRA_SELECTED_IMAGE_URIS]!!
        val nScreenshots = screenshotUris.size

        val cropBundles = mutableListOf<CropBundle>()
        val cropResults = CropResults()
        val liveProgress: LiveData<Int> = MutableLiveData(0)

        suspend fun cropCoroutine(
            context: Context,
            onFinishedListener: () -> Unit
        ) {
            coroutineScope {
                getImminentUris().forEach { uri ->
                    withContext(Dispatchers.IO) {
                        getCropBundle(uri, context)?.let {
                            cropBundles.add(it)
                        }
                    }
                    withContext(Dispatchers.Main) {
                        liveProgress.increment()
                    }
                }
                onFinishedListener()
            }
        }

        private fun getImminentUris(): List<Uri> =
            screenshotUris.run {
                subList(liveProgress.value!!, size)
            }

        private fun getCropBundle(screenshotUri: Uri, context: Context): CropBundle? {
            i { "getCropBundle $screenshotUri" }

            val mediaUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                getMediaUri(context, screenshotUri)!!
            else
                screenshotUri

            context.contentResolver.loadBitmap(mediaUri)?.let { screenshotBitmap ->
                screenshotBitmap.cropEdgesCandidates()?.let { candidates ->
                    return CropBundle.assemble(
                        Screenshot(
                            mediaUri,
                            screenshotBitmap.height,
                            candidates,
                            Screenshot.MediaStoreData.query(context.contentResolver, mediaUri)
                        ),
                        screenshotBitmap,
                        candidates.maxHeightEdges()
                    )
                }
                    ?: run {
                        cropResults.nNotCroppableImages += 1
                        return null
                    }
            }
                ?: run {
                    cropResults.nNotOpenableImages += 1
                    return null
                }
        }
    }

    private val viewModel by viewModels<ViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.populate()
    }

    private fun CropBinding.populate() {
        croppingProgressBar.max = viewModel.nScreenshots
        progressTv.max = viewModel.nScreenshots

        viewModel.liveProgress.observe(viewLifecycleOwner) {
            progressTv.update(it)
            croppingProgressBar.progress = it
        }
    }

    override fun onResume() {
        super.onResume()

        lifecycleScope.launch {
            viewModel.cropCoroutine(requireContext()) {
                invokeSubsequentScreen()
            }
        }
    }

    private fun invokeSubsequentScreen() {
        if (viewModel.cropBundles.isNotEmpty())
            launchCropExamination()
        else
            launchAfterShortDelay {  // to assure progress bar having reached 100% before UI change
                requireViewBoundFragmentActivity()
                    .fragmentReplacementTransaction(CroppingFailedFragment())
                    .commit()
            }
    }

    /**
     * Inherently sets [ExaminationActivity.ViewModel.cropBundles]
     */
    private fun launchCropExamination() {
        ExaminationActivity.ViewModel.cropBundles = viewModel.cropBundles

        startActivity(
            Intent(requireContext(), ExaminationActivity::class.java)
                .putExtra(
                    CropResults.EXTRA,
                    viewModel.cropResults
                )
        )
        Animatoo.animateSwipeLeft(requireActivity())
    }

    fun onBackPress() {
        viewModel.backPressListener(
            {
                requireContext().showToast("Tap again to cancel")
            },
            {
                MainActivity.start(requireActivity())
            }
        )
    }
}