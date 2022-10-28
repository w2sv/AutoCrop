package com.autocrop.activities.main.fragments.about

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.autocrop.activities.main.fragments.MainActivityFragment
import com.autocrop.preferences.BooleanPreferences
import com.autocrop.utils.android.extensions.animate
import com.autocrop.utils.android.extensions.buildAndShow
import com.autocrop.utils.android.extensions.getLong
import com.autocrop.utils.android.extensions.snackyBuilder
import com.daimajia.androidanimations.library.Techniques
import com.w2sv.autocrop.R
import com.w2sv.autocrop.databinding.FragmentAboutBinding

class AboutFragment:
    MainActivityFragment<FragmentAboutBinding>(FragmentAboutBinding::class.java){

    override fun onResume() {
        super.onResume()

        if (!BooleanPreferences.aboutFragmentInstructionsShown)
            Handler(Looper.getMainLooper()).postDelayed(
                {
                    requireActivity()
                        .snackyBuilder(
                            "Pro tip: check out what happens if you click on the various view elements",
                            duration = resources.getInteger(R.integer.duration_snackbar_long)
                        )
                        .setIcon(R.drawable.ic_outline_info_24)
                        .buildAndShow()
                },
                resources.getLong(R.integer.delay_medium)
            )
    }

    override fun onViewCreatedCore(savedInstanceState: Bundle?) {
        binding.setOnClickListeners()

        viewLifecycleOwner.lifecycle.addObserver(binding.copyrightTv)
    }

    private fun FragmentAboutBinding.setOnClickListeners(){
        appTitleTextView.setOnClickListener { it.animate(Techniques.Wobble) }
        logoIv.setOnClickListener { it.animate(Techniques.Tada) }
        versionTv.setOnClickListener { it.animate(Techniques.RubberBand) }
    }
}