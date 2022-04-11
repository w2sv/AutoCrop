package com.autocrop.activities.cropping.fragments.cropping

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.lifecycle.lifecycleScope
import com.autocrop.activities.ActivityTransitions
import com.autocrop.activities.IntentIdentifier
import com.autocrop.activities.cropping.fragments.CroppingActivityFragment
import com.autocrop.activities.examination.ExaminationActivity
import com.autocrop.activities.examination.ExaminationActivityViewModel
import com.autocrop.types.CropBundle
import com.autocrop.utils.executeAsyncTask
import com.autocrop.utils.logBeforehand
import com.autocrop.utils.toInt
import com.w2sv.autocrop.databinding.ActivityCroppingFragmentRootBinding
import kotlinx.coroutines.Job

class CroppingFragment
    : CroppingActivityFragment<ActivityCroppingFragmentRootBinding>() {

    private lateinit var croppingJob: Job

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        croppingJob = lifecycleScope.executeAsyncTask(::doInBackground, ::onProgressUpdate) { onPostExecute() }
    }

    private suspend fun doInBackground(publishProgress: suspend (Pair<Int, Int>) -> Unit): Void?{
        var decimalStepSum = 0f

        sharedViewModel.uris.forEachIndexed { index, uri ->
            // attempt to crop image, add uri-crop mapping to image cash if successful
            with(croppedImage(image = BitmapFactory.decodeStream(requireContext().contentResolver.openInputStream(uri))!!)) {
                this?.let {
                    sharedViewModel.cropBundles.add(
                        CropBundle(uri, first, second, third)
                    )
                }
            }

            // advance progress bar, screenshot number text view
            decimalStepSum += sharedViewModel.progressBarDecimalStep
            with(Pair(index + 1, sharedViewModel.progressBarIntStep + (decimalStepSum >= 1).toInt())) {
                if (second > sharedViewModel.progressBarIntStep)
                    decimalStepSum -= 1

                publishProgress(this)
            }
        }
        return null
    }

    private fun onProgressUpdate(imageOrdinalWithProgressBarStep: Pair<Int, Int>) {
        binding.croppingCurrentImageNumberTextView.updateText(imageOrdinalWithProgressBarStep.first)
        binding.croppingProgressBar.incrementProgressBy(imageOrdinalWithProgressBarStep.second)
    }

    /**
     * Starts either Examination- or MainActivity depending on whether or not any
     * of the selected images has been successfully cropped
     */
    private fun onPostExecute() = logBeforehand("Async Cropping task finished") {
        if (sharedViewModel.cropBundles.isNotEmpty())
            startExaminationActivity()
        else
            // delay briefly to assure progress bar having reached 100% before UI change
            Handler(Looper.getMainLooper()).postDelayed(
                {
                    with(typedActivity){
                        replaceCurrentFragmentWith(croppingUnsuccessfulFragment)
                    }
                },
                300
            )
    }

    private fun startExaminationActivity(){
        ExaminationActivityViewModel.cropBundles = sharedViewModel.cropBundles

        requireActivity().let { activity ->
            startActivity(
                Intent(activity, ExaminationActivity::class.java).putExtra(
                    IntentIdentifier.N_DISMISSED_IMAGES,
                    sharedViewModel.nDismissedImages
                )
            )
            ActivityTransitions.PROCEED(activity)
        }
    }

    override fun onStop() {
        super.onStop()

        croppingJob.cancel()
    }
}