package com.w2sv.autocrop.activities.crop.fragments.cropping

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import com.blogspot.atifsoftwares.animatoolib.Animatoo
import com.w2sv.androidutils.BackPressListener
import com.w2sv.androidutils.extensions.getLong
import com.w2sv.androidutils.extensions.postValue
import com.w2sv.autocrop.R
import com.w2sv.autocrop.activities.ApplicationFragment
import com.w2sv.autocrop.activities.crop.CropActivity
import com.w2sv.autocrop.activities.crop.fragments.croppingfailed.CroppingFailedFragment
import com.w2sv.autocrop.activities.cropexamination.CropExaminationActivity
import com.w2sv.autocrop.activities.main.MainActivity
import com.w2sv.autocrop.cropbundle.CropBundle
import com.w2sv.autocrop.cropbundle.Screenshot
import com.w2sv.autocrop.cropbundle.cropping.cropEdgesCandidates
import com.w2sv.autocrop.cropbundle.cropping.maxHeightEdges
import com.w2sv.autocrop.cropbundle.io.extensions.loadBitmap
import com.w2sv.autocrop.databinding.FragmentCropBinding
import com.w2sv.autocrop.utils.extensions.snackyBuilder
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class CropFragment
    : ApplicationFragment<FragmentCropBinding>(FragmentCropBinding::class.java) {

    companion object {
        fun getInstance(screenshotUris: List<Uri>): CropFragment =
            CropFragment().apply {
                arguments = bundleOf(MainActivity.EXTRA_SELECTED_IMAGE_URIS to screenshotUris)
            }
    }

    @HiltViewModel
    class ViewModel @Inject constructor(
        savedStateHandle: SavedStateHandle,
        @ApplicationContext context: Context
    ) : androidx.lifecycle.ViewModel() {

        val backPressListener = BackPressListener(
            viewModelScope,
            context.resources.getLong(R.integer.duration_backpress_confirmation_window)
        )

        private val screenshotUris: List<Uri> = savedStateHandle[MainActivity.EXTRA_SELECTED_IMAGE_URIS]!!
        val nScreenshots = screenshotUris.size

        fun getNUncroppableImages(): Int =
            nScreenshots - cropBundles.size

        val cropBundles = mutableListOf<CropBundle>()
        var nNotOpenableUris = 0

        val liveProgress: LiveData<Int> = MutableLiveData(0)

        suspend fun launchCropCoroutine(
            contentResolver: ContentResolver,
            onFinishedListener: () -> Unit
        ) {
            coroutineScope {
                launch {
                    getImminentUris().forEach { uri ->
                        withContext(Dispatchers.IO) {
                            getCropBundle(uri, contentResolver)?.let {
                                cropBundles.add(it)
                            }
                        }
                        withContext(Dispatchers.Main) {
                            with(liveProgress) {
                                postValue(value!! + 1)
                            }
                        }
                    }
                    onFinishedListener()
                }
            }
        }

        private fun getImminentUris(): List<Uri> =
            screenshotUris.run {
                subList(liveProgress.value!!, size)
            }

        private fun getCropBundle(screenshotUri: Uri, contentResolver: ContentResolver): CropBundle? =
            contentResolver.loadBitmap(screenshotUri)?.let { screenshotBitmap ->
                screenshotBitmap.cropEdgesCandidates()?.let { candidates ->
                    CropBundle.assemble(
                        Screenshot(
                            screenshotUri,
                            screenshotBitmap.height,
                            candidates,
                            Screenshot.MediaStoreData.query(contentResolver, screenshotUri)
                        ),
                        screenshotBitmap,
                        candidates.maxHeightEdges()
                    )
                }
            }
                ?: null.also {
                    nNotOpenableUris += 1
                }
    }

    private val viewModel by viewModels<ViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.initialize()
    }

    private fun FragmentCropBinding.initialize() {
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
            viewModel.launchCropCoroutine(requireContext().contentResolver) {
                invokeSubsequentScreen()
            }
        }
    }

    private fun invokeSubsequentScreen() {
        if (viewModel.cropBundles.isNotEmpty())
            launchCropExamination()
        else
            launchAfterShortDelay {  // to assure progress bar having reached 100% before UI change
                getFragmentHostingActivity()
                    .fragmentReplacementTransaction(CroppingFailedFragment())
                    .commit()
            }
    }

    /**
     * Inherently sets [CropExaminationActivity.ViewModel.cropBundles]
     */
    private fun launchCropExamination() {
        CropExaminationActivity.ViewModel.cropBundles = viewModel.cropBundles

        startActivity(
            Intent(requireContext(), CropExaminationActivity::class.java)
                .putExtra(
                    CropActivity.EXTRA_N_UNCROPPED_SCREENSHOTS,
                    viewModel.getNUncroppableImages()
                )
                .putExtra(
                    CropActivity.EXTRA_N_NOT_OPENABLE_URIS,
                    viewModel.nNotOpenableUris
                )
        )
        Animatoo.animateSwipeLeft(requireActivity())
    }

    fun onBackPress() {
        viewModel.backPressListener(
            {
                requireActivity()
                    .snackyBuilder("Tap again to cancel")
                    .build()
                    .show()
            },
            {
                MainActivity.restart(requireActivity())
            }
        )
    }
}