package com.w2sv.autocrop.activities.crop

import android.os.Bundle
import android.view.View
import com.w2sv.autocrop.activities.AppFragment
import com.w2sv.autocrop.activities.main.MainActivity
import com.w2sv.autocrop.databinding.CroppingFailedBinding

class CroppingFailedFragment
    : AppFragment<CroppingFailedBinding>(CroppingFailedBinding::class.java) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.gotItButton.setOnClickListener {
            MainActivity.start(requireContext())
        }
    }
}