package com.w2sv.autocrop.activities.crop.fragments.croppingfailed

import android.os.Bundle
import android.view.View
import com.w2sv.autocrop.activities.crop.CropActivity
import com.w2sv.autocrop.controller.ApplicationFragment
import com.w2sv.autocrop.databinding.FragmentCroppingFailedBinding

class CroppingFailedFragment
    : ApplicationFragment<FragmentCroppingFailedBinding>(FragmentCroppingFailedBinding::class.java) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.gotItButton.setOnClickListener {
            castActivity<CropActivity>().startMainActivity()
        }
    }
}