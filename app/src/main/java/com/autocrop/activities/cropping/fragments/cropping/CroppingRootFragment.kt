package com.autocrop.activities.cropping.fragments.cropping

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.autocrop.activities.IntentIdentifiers
import com.autocrop.activities.cropping.CroppingActivity
import com.autocrop.activities.cropping.CroppingActivityViewModel
import com.autocrop.activities.cropping.fragments.croppingunsuccessful.CroppingUnsuccessfulFragment
import com.autocrop.activities.examination.ExaminationActivity
import com.autocrop.utils.android.proceedTransitionAnimation
import com.autocrop.utils.logBeforehand
import com.w2sv.autocrop.databinding.ActivityCroppingFragmentRootBinding
import java.lang.ref.WeakReference

class CroppingRootFragment: Fragment() {
    private val viewModel: CroppingActivityViewModel by activityViewModels()

    lateinit var cropper: Cropper

    private var _binding: ActivityCroppingFragmentRootBinding? = null
    private val binding: ActivityCroppingFragmentRootBinding
        get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ActivityCroppingFragmentRootBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

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
        if (ExaminationActivity.cropBundles.isNotEmpty())
            startExaminationActivity(viewModel.nSelectedImages - ExaminationActivity.cropBundles.size)
        else
            (requireActivity() as CroppingActivity).replaceCurrentFragmentWith(
                CroppingUnsuccessfulFragment()
            )
    }

    private fun startExaminationActivity(nDismissedCrops: Int) =
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