package com.w2sv.autocrop.ui.screen.croppingfailed

import android.os.Bundle
import android.view.View
import com.w2sv.autocrop.AppFragment
import com.w2sv.autocrop.databinding.CroppingFailedBinding

class CroppingFailedScreenFragment
    : AppFragment<CroppingFailedBinding>(CroppingFailedBinding::class.java) {

    override val onBackPressed: () -> Unit
        get() = ::navigateToMainActivity

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.gotItButton.setOnClickListener {
            navigateToMainActivity()
        }
    }

    private fun navigateToMainActivity() {
        navController.navigate(CroppingFailedScreenFragmentDirections.navigateToHomeScreen())
    }
}