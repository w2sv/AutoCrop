package com.autocrop.activities.cropping.fragments.cropping

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import com.autocrop.activities.IntentIdentifiers
import com.autocrop.activities.cropping.fragments.CroppingActivityFragment
import com.autocrop.activities.cropping.fragments.croppingunsuccessful.CroppingUnsuccessfulFragment
import com.autocrop.activities.examination.ExaminationActivity
import com.autocrop.utils.android.proceedTransitionAnimation
import com.autocrop.utils.logBeforehand
import com.w2sv.autocrop.databinding.ActivityCroppingFragmentRootBinding
import java.lang.ref.WeakReference

class CroppingFragment
    : CroppingActivityFragment<ActivityCroppingFragmentRootBinding>(ActivityCroppingFragmentRootBinding::inflate) {

    lateinit var cropper: Cropper

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        cropper = Cropper(
            viewModel,
            WeakReference(binding.croppingProgressBar),
            WeakReference(binding.croppingCurrentImageNumberTextView),
            requireActivity().contentResolver,
            ::onTaskCompleted
        ).apply {
            execute(*viewModel.uris.toTypedArray())
        }
    }

    /**
     * Starts either Examination- or MainActivity depending on whether or not any
     * of the selected images has been successfully cropped
     */
    private fun onTaskCompleted() = logBeforehand("Async Cropping task finished") {
        if (viewModel.cropBundles.isNotEmpty())
            startExaminationActivity(viewModel.nSelectedImages - viewModel.cropBundles.size)
        else
            // delay briefly to assure progress bar having reached 100% before UI change
            Handler(Looper.getMainLooper()).postDelayed(
                {
                    activity.replaceCurrentFragmentWith(
                        CroppingUnsuccessfulFragment()
                    )
                },
                300
            )
    }

    private fun startExaminationActivity(nDismissedCrops: Int){
        ExaminationActivity.cropBundles = viewModel.cropBundles

        requireActivity().let { activity ->
            startActivity(
                Intent(activity, ExaminationActivity::class.java).putExtra(
                    IntentIdentifiers.N_DISMISSED_IMAGES,
                    nDismissedCrops
                )
            )
            activity.proceedTransitionAnimation()
        }
    }
}