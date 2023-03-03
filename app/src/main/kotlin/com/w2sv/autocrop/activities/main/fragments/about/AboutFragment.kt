package com.w2sv.autocrop.activities.main.fragments.about

import android.os.Bundle
import android.view.View
import com.daimajia.androidanimations.library.Techniques
import com.w2sv.autocrop.activities.AppFragment
import com.w2sv.autocrop.databinding.AboutBinding
import com.w2sv.autocrop.ui.views.animate

class AboutFragment :
    AppFragment<AboutBinding>(AboutBinding::class.java) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.setOnClickListeners()
    }

    private fun AboutBinding.setOnClickListeners() {
        appTitleTextView.setOnClickListener { it.animate(Techniques.Wobble) }
        logoIv.setOnClickListener { it.animate(Techniques.Tada) }
        versionTv.setOnClickListener { it.animate(Techniques.RubberBand) }
    }
}