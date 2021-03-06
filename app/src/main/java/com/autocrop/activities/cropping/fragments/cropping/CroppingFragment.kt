package com.autocrop.activities.cropping.fragments.cropping

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.lifecycle.lifecycleScope
import com.autocrop.activities.ActivityTransitions
import com.autocrop.activities.IntentExtraIdentifier
import com.autocrop.activities.cropping.fragments.CroppingActivityFragment
import com.autocrop.activities.cropping.fragments.croppingfailed.CroppingFailedFragment
import com.autocrop.activities.examination.ExaminationActivity
import com.autocrop.activities.examination.ExaminationActivityViewModel
import com.autocrop.collections.CropBundle
import com.autocrop.utils.executeAsyncTask
import com.autocrop.utils.logBeforehand
import com.autocrop.utilsandroid.openBitmap
import com.lyrebirdstudio.croppylib.utils.extensions.asMutable
import com.w2sv.autocrop.R
import com.w2sv.autocrop.databinding.CroppingFragmentBinding
import kotlinx.coroutines.Job

class CroppingFragment
    : CroppingActivityFragment<CroppingFragmentBinding>(CroppingFragmentBinding::class.java) {

    private lateinit var croppingJob: Job

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // attach views to currentCropNumber observable
        sharedViewModel.currentImageNumber.observe(viewLifecycleOwner) {
            binding.croppingCurrentImageNumberTextView.updateText(it)
            binding.croppingProgressBar.progress = it
        }

        // launch croppingJob
        croppingJob = lifecycleScope.executeAsyncTask(
            ::cropImages,
            { sharedViewModel.currentImageNumber.increment()},
            { startExaminationActivityOrInvokeCroppingFailureFragment() }
        )
    }

    private suspend fun cropImages(publishProgress: suspend (Void?) -> Unit): Void?{
        sharedViewModel.uris.subList(sharedViewModel.currentImageNumber.value!!, sharedViewModel.uris.size).forEach { uri ->

            // attempt to crop image, upon success add resulting CropBundle to sharedViewModel
            val screenshotBitmap = requireContext().contentResolver.openBitmap(uri)

            cropRect(screenshotBitmap)?.let { cropRect ->
                sharedViewModel.cropBundles.add(
                    CropBundle(
                        uri,
                        screenshotBitmap,
                        cropRect
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
    private fun startExaminationActivityOrInvokeCroppingFailureFragment() = logBeforehand("Async Cropping task finished") {
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
     * Inherently sets [ExaminationActivityViewModel.cropBundles]
     */
    private fun startExaminationActivity(){
        ExaminationActivityViewModel.cropBundles = sharedViewModel.cropBundles

        requireActivity().let { activity ->
            startActivity(
                Intent(activity, ExaminationActivity::class.java).putExtra(
                    IntentExtraIdentifier.N_DISMISSED_IMAGES,
                    sharedViewModel.nDismissedImages
                )
            )
            ActivityTransitions.PROCEED(activity)
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