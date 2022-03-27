package com.autocrop.activities.cropping.fragments.croppingunsuccessful

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.autocrop.activities.cropping.CroppingActivity
import com.autocrop.activities.cropping.CroppingActivityViewModel
import com.autocrop.utils.android.hideSystemUI
import com.w2sv.autocrop.databinding.ActivityCroppingFragmentCroppingUnsuccessfulBinding

class CroppingUnsuccessfulFragment: Fragment(){

    private var _binding: ActivityCroppingFragmentCroppingUnsuccessfulBinding? = null
    private val binding: ActivityCroppingFragmentCroppingUnsuccessfulBinding
        get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ActivityCroppingFragmentCroppingUnsuccessfulBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private val viewModel: CroppingActivityViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Handler(Looper.getMainLooper()).postDelayed(
            {
                hideSystemUI(requireActivity().window)

                binding.croppingUnsuccessfulTextView.updateText(viewModel.nSelectedImages > 1)

                Handler(Looper.getMainLooper()).postDelayed(
                    { (requireActivity() as CroppingActivity).startMainActivity() },
                    3000
                )
            },
            300
        )
    }
}