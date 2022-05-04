package com.autocrop.activities.cropping.fragments.croppingfailed

import android.os.Bundle
import android.view.View
import com.autocrop.activities.cropping.fragments.CroppingActivityFragment
import com.w2sv.autocrop.databinding.CroppingFragmentFailedBinding

class CroppingFailedFragment
    : CroppingActivityFragment<CroppingFragmentFailedBinding>(CroppingFragmentFailedBinding::class.java) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        binding.croppingUnsuccessfulGotItButton.setOnClickListener {
            castedActivity.returnToMainActivity()
        }
    }
}