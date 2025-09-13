package com.w2sv.autocrop.ui.screen.croppingfailed

import android.os.Bundle
import android.view.View
import com.w2sv.autocrop.databinding.CroppingFailedBinding
import com.w2sv.autocrop.ui.ViewBoundAppFragment

class CroppingFailedScreenFragment : ViewBoundAppFragment<CroppingFailedBinding>(CroppingFailedBinding::class.java) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.gotItButton.setOnClickListener {
            navController.popBackStack()
        }
    }
}
