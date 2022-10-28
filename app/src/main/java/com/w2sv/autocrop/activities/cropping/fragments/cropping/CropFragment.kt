package com.w2sv.autocrop.activities.cropping.fragments.cropping

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import com.w2sv.autocrop.CropBundle
import com.w2sv.autocrop.Screenshot
import com.w2sv.autocrop.activities.cropping.cropping.cropEdgesCandidates
import com.w2sv.autocrop.activities.cropping.cropping.maxHeightEdges
import com.w2sv.autocrop.activities.cropping.fragments.CropActivityFragment
import com.w2sv.autocrop.activities.cropping.fragments.croppingfailed.CroppingFailedFragment
import com.w2sv.autocrop.activities.iodetermination.IODeterminationActivity
import com.w2sv.autocrop.activities.iodetermination.IODeterminationActivityViewModel
import com.w2sv.autocrop.activities.main.MainActivity
import com.w2sv.autocrop.utils.android.extensions.getLong
import com.w2sv.autocrop.utils.android.extensions.loadBitmap
import com.w2sv.autocrop.utils.android.postDelayed
import com.w2sv.autocrop.utils.kotlin.extensions.executeAsyncTaskWithProgressUpdateReceiver
import com.blogspot.atifsoftwares.animatoolib.Animatoo
import com.w2sv.autocrop.R
import com.w2sv.autocrop.databinding.FragmentCropBinding
import de.paul_woitaschek.slimber.i
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
            { startIODeterminationActivityOrInvokeCroppingFailureFragment() }
        )
    }

    private suspend fun cropImages(publishProgress: suspend (Void?) -> Unit): Void? {
        sharedViewModel.uris.subList(
            sharedViewModel.liveImageNumber.value!!,
            sharedViewModel.uris.size
        ).forEach { uri ->
            // attempt to crop image; upon success add CropBundle to sharedViewModel
            val bitmap = requireContext().contentResolver.loadBitmap(uri)

            bitmap.cropEdgesCandidates()?.let { candidates ->
                sharedViewModel.cropBundles.add(
                    CropBundle.assemble(
                        Screenshot(
                            uri,
                            bitmap.height,
                            candidates,
                            Screenshot.MediaStoreData.query(requireContext().contentResolver, uri)
                        ),
                        bitmap,
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
    private fun startIODeterminationActivityOrInvokeCroppingFailureFragment() {
        i { "Async Cropping task finished" }

        if (sharedViewModel.cropBundles.isNotEmpty())
            startIODeterminationActivity()
        else
        // delay briefly to assure progress bar having reached 100% before UI change
            postDelayed(resources.getLong(R.integer.delay_small)){
                fragmentHostingActivity
                    .fragmentReplacementTransaction(CroppingFailedFragment())
                    .commit()
            }
    }

    /**
     * Inherently sets [IODeterminationActivityViewModel.cropBundles]
     */
    private fun startIODeterminationActivity() {
        IODeterminationActivityViewModel.cropBundles = sharedViewModel.cropBundles

        requireActivity().let { activity ->
            startActivity(
                Intent(activity, IODeterminationActivity::class.java).putExtra(
                    MainActivity.EXTRA_N_DISMISSED_IMAGES,
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