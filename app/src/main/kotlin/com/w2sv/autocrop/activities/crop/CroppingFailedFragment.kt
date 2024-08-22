package com.w2sv.autocrop.activities.crop

import android.content.Context
import android.os.Bundle
import android.view.View
import com.w2sv.autocrop.activities.AppFragment
import com.w2sv.autocrop.activities.main.MainActivity
import com.w2sv.autocrop.databinding.CroppingFailedBinding
import com.w2sv.autocrop.utils.registerOnBackPressedHandler

class CroppingFailedFragment
    : AppFragment<CroppingFailedBinding>(CroppingFailedBinding::class.java) {

    override fun onAttach(context: Context) {
        super.onAttach(context)

        registerOnBackPressedHandler {
            navigateToMainActivity()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.gotItButton.setOnClickListener {
            navigateToMainActivity()
        }
    }

    private fun navigateToMainActivity() {
        MainActivity.start(requireActivity())
    }
}