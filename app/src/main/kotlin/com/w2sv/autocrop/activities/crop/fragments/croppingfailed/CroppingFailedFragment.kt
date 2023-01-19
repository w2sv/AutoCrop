package com.w2sv.autocrop.activities.crop.fragments.croppingfailed

import android.os.Bundle
import android.view.View
import com.w2sv.autocrop.activities.AppFragment
import com.w2sv.autocrop.activities.main.MainActivity
import com.w2sv.autocrop.databinding.FragmentCroppingFailedBinding

class CroppingFailedFragment
    : AppFragment<FragmentCroppingFailedBinding>(FragmentCroppingFailedBinding::class.java) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.gotItButton.setOnClickListener {
            MainActivity.start(requireActivity())
        }
    }
}