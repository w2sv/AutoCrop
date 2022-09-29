package com.autocrop.activities.cropping.fragments.croppingfailed

import android.os.Bundle
import android.view.View
import com.autocrop.activities.cropping.fragments.CropActivityFragment
import com.autocrop.ui.controller.activity.startMainActivity
import com.w2sv.autocrop.databinding.FragmentCroppingFailedBinding

class CroppingFailedFragment
    : CropActivityFragment<FragmentCroppingFailedBinding>(FragmentCroppingFailedBinding::class.java) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        binding.croppingUnsuccessfulGotItButton.setOnClickListener {
            requireActivity().startMainActivity()
        }
    }
}