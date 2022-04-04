package com.autocrop.activities.cropping.fragments.cropping

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import com.autocrop.activities.ActivityTransitions
import com.autocrop.activities.IntentIdentifiers
import com.autocrop.activities.cropping.fragments.CroppingActivityFragment
import com.autocrop.activities.examination.ExaminationActivity
import com.autocrop.utils.logBeforehand
import com.w2sv.autocrop.databinding.ActivityCroppingFragmentRootBinding
import java.lang.ref.WeakReference

class CroppingFragment
    : CroppingActivityFragment<ActivityCroppingFragmentRootBinding>() {

    lateinit var cropper: Cropper

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        cropper = Cropper(
            sharedViewModel,
            WeakReference(binding.croppingProgressBar),
            WeakReference(binding.croppingCurrentImageNumberTextView),
            requireActivity().contentResolver,
            ::onTaskCompleted
        ).apply {
            execute(*sharedViewModel.uris.toTypedArray())
        }
    }

    /**
     * Starts either Examination- or MainActivity depending on whether or not any
     * of the selected images has been successfully cropped
     */
    private fun onTaskCompleted() = logBeforehand("Async Cropping task finished") {
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
        ExaminationActivity.cropBundles = sharedViewModel.cropBundles

        requireActivity().let { activity ->
            startActivity(
                Intent(activity, ExaminationActivity::class.java).putExtra(
                    IntentIdentifiers.N_DISMISSED_IMAGES,
                    sharedViewModel.nDismissedImages
                )
            )
            ActivityTransitions.PROCEED(activity)
        }
    }
}