package com.autocrop.activities.cropping.fragments.cropping

import android.content.Intent
import android.graphics.BitmapFactory
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
import com.autocrop.types.CropBundle
import com.autocrop.utils.executeAsyncTask
import com.autocrop.utils.logBeforehand
import com.w2sv.autocrop.databinding.CroppingFragmentBinding
import kotlinx.coroutines.Job

class CroppingFragment
    : CroppingActivityFragment<CroppingFragmentBinding>() {

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
            { sharedViewModel.incrementCurrentImageNumber() },
            { startExaminationActivityOrInvokeCroppingFailureFragment() }
        )
    }

    private suspend fun cropImages(publishProgress: suspend (Void?) -> Unit): Void?{
        sharedViewModel.uris.subList(sharedViewModel.currentImageNumber.value!!, sharedViewModel.uris.size).forEach { uri ->

            // attempt to crop image, upon success add resulting CropBundle to sharedViewModel
            croppedImage(BitmapFactory.decodeStream(requireContext().contentResolver.openInputStream(uri))!!)?.run {
                sharedViewModel.cropBundles.add(
                    CropBundle(uri, first, second, third)
                )
            }

            // advance progress bar, screenshot number text view
            publishProgress(null)
        }
        return null
    }

    /**
     * Starts either Examination- or MainActivity depending on whether or not any
     * of the selected images has been successfully cropped
     */
    private fun startExaminationActivityOrInvokeCroppingFailureFragment() = logBeforehand("Async Cropping task finished") {
        if (sharedViewModel.cropBundles.isNotEmpty())
            startExaminationActivity()
        else
            // delay briefly to assure progress bar having reached 100% before UI change
            Handler(Looper.getMainLooper()).postDelayed(
                { typedActivity.replaceCurrentFragmentWith(CroppingFailedFragment()) },
                300
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