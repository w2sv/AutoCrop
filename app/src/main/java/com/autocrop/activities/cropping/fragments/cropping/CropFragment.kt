package com.autocrop.activities.cropping.fragments.cropping

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.lifecycle.lifecycleScope
import com.autocrop.activities.IntentExtraIdentifier
import com.autocrop.activities.cropping.cropping.cropEdgesCandidates
import com.autocrop.activities.cropping.cropping.maxHeightEdges
import com.autocrop.activities.cropping.fragments.CropActivityFragment
import com.autocrop.activities.cropping.fragments.croppingfailed.CroppingFailedFragment
import com.autocrop.activities.iodetermination.IODeterminationActivity
import com.autocrop.activities.iodetermination.IODeterminationActivityViewModel
import com.autocrop.dataclasses.CropBundle
import com.autocrop.dataclasses.Screenshot
import com.autocrop.utils.android.extensions.openBitmap
import com.autocrop.utils.kotlin.extensions.executeAsyncTaskWithProgressUpdateReceiver
import com.autocrop.utils.kotlin.logBeforehand
import com.blogspot.atifsoftwares.animatoolib.Animatoo
import com.w2sv.autocrop.R
import com.w2sv.autocrop.databinding.FragmentCropBinding
import kotlinx.coroutines.Job

class CropFragment
    : CropActivityFragment<FragmentCropBinding>(FragmentCropBinding::class.java) {

    private lateinit var croppingJob: Job

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // attach views to currentCropNumber observable
        sharedViewModel.liveImageNumber.observe(viewLifecycleOwner) {
            binding.progressTv.update(it)
            binding.croppingProgressBar.progress = it
        }

        // launch croppingJob
        croppingJob = lifecycleScope.executeAsyncTaskWithProgressUpdateReceiver(
            ::cropImages,
            { sharedViewModel.liveImageNumber.increment() },
            { startExaminationActivityOrInvokeCroppingFailureFragment() }
        )
    }

    private suspend fun cropImages(publishProgress: suspend (Void?) -> Unit): Void? {
        sharedViewModel.uris.subList(
            sharedViewModel.liveImageNumber.value!!,
            sharedViewModel.uris.size
        ).forEach { uri ->

            // attempt to crop image; upon success add CropBundle to sharedViewModel
            val screenshotBitmap = requireContext().contentResolver.openBitmap(uri)

            screenshotBitmap.cropEdgesCandidates()?.let { candidates ->
                sharedViewModel.cropBundles.add(
                    CropBundle.assemble(
                        Screenshot.fromContentResolver(
                            requireContext().contentResolver,
                            uri,
                            candidates
                        ),
                        screenshotBitmap,
                        candidates.maxHeightEdges()
                    )
                )
            }

            // advance progress bar, screenshot number text view
            publishProgress(null)
        }
        return null
    }

    /**
     * Starts either Examination- or MainActivity depending on whether or not any
     * of the selected images has been successfully cropRect
     */
    private fun startExaminationActivityOrInvokeCroppingFailureFragment() =
        logBeforehand("Async Cropping task finished") {
            if (sharedViewModel.cropBundles.isNotEmpty())
                startExaminationActivity()
            else
            // delay briefly to assure progress bar having reached 100% before UI change
                Handler(Looper.getMainLooper()).postDelayed(
                    { fragmentHostingActivity.replaceCurrentFragmentWith(CroppingFailedFragment()) },
                    resources.getInteger(R.integer.delay_minimal).toLong()
                )
        }

    /**
     * Inherently sets [IODeterminationActivityViewModel.cropBundles]
     */
    private fun startExaminationActivity() {
        IODeterminationActivityViewModel.cropBundles = sharedViewModel.cropBundles

        requireActivity().let { activity ->
            startActivity(
                Intent(activity, IODeterminationActivity::class.java).putExtra(
                    IntentExtraIdentifier.N_DISMISSED_IMAGES,
                    sharedViewModel.nDismissedImages
                )
            )
            Animatoo.animateSwipeLeft(activity)
        }
    }

    /**
     * Cancel [croppingJob]
     */
    override fun onStop() {
        super.onStop()

        croppingJob.cancel()
    }
}