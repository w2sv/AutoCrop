package com.w2sv.autocrop.activities.crop

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
import com.w2sv.androidutils.eventhandling.BackPressHandler
import com.w2sv.androidutils.lifecycle.increment
import com.w2sv.androidutils.notifying.showToast
import com.w2sv.androidutils.ui.resources.getLong
import com.w2sv.autocrop.R
import com.w2sv.autocrop.activities.AppFragment
import com.w2sv.autocrop.activities.ViewBoundFragmentActivity
import com.w2sv.autocrop.activities.crop.domain.CropResults
import com.w2sv.autocrop.activities.examination.ExaminationActivity
import com.w2sv.autocrop.activities.main.MainActivity
import com.w2sv.autocrop.databinding.CropBinding
import com.w2sv.autocrop.utils.extensions.launchAfterShortDelay
import com.w2sv.autocrop.utils.extensions.startMainActivity
import com.w2sv.autocrop.utils.getFragment
import com.w2sv.autocrop.utils.getMediaUri
import com.w2sv.autocrop.utils.requireCastActivity
import com.w2sv.cropbundle.CropBundle
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
            i { "getCropBundle; screenshotUri=$screenshotUri" }

            return CropBundle.attemptCreation(
                screenshotMediaUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                    getMediaUri(context, screenshotUri)!!
                else
                    screenshotUri,
                context = context
            ).run {
                when {
                    first != null -> first
                    second == CropBundle.CreationFailureReason.BitmapLoadingFailure -> {
                        cropResults.nNotOpenableImages += 1
                        null
                    }

                    second == CropBundle.CreationFailureReason.NoCropEdgesFound -> {
                        cropResults.nNotCroppableImages += 1
                        null
                    }

                    else -> throw Exception()
                }
            }
        }
    }

    private val viewModel by viewModels<ViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.populate()
        viewModel.setObservers()
    }

    private fun CropBinding.populate() {
        croppingProgressBar.max = viewModel.nScreenshots
    }

    private fun ViewModel.setObservers() {
        liveProgress.observe(viewLifecycleOwner) {
            binding.progressTv.updateText(it, nScreenshots)
            binding.croppingProgressBar.progress = it
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
                requireCastActivity<ViewBoundFragmentActivity>()
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
                requireContext().showToast(getString(R.string.tap_again_to_cancel))
            },
            {
                requireActivity().startMainActivity()
            }
        )
    }
}