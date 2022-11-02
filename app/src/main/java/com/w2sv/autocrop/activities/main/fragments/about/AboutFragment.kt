package com.w2sv.autocrop.activities.main.fragments.about

import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import com.daimajia.androidanimations.library.Techniques
import com.w2sv.autocrop.R
import com.w2sv.autocrop.activities.main.fragments.MainActivityFragment
import com.w2sv.autocrop.databinding.FragmentAboutBinding
import com.w2sv.autocrop.preferences.BooleanPreferences
import com.w2sv.autocrop.utils.android.extensions.animate
import com.w2sv.autocrop.utils.android.extensions.getLong
import com.w2sv.autocrop.utils.android.extensions.snackyBuilder
import com.w2sv.kotlinutils.extensions.launchDelayed

class AboutFragment :
    MainActivityFragment<FragmentAboutBinding>(FragmentAboutBinding::class.java) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (!BooleanPreferences.aboutFragmentInstructionsShown)
            lifecycleScope.launchDelayed(resources.getLong(R.integer.delay_medium)){
                requireActivity()
                    .snackyBuilder(
                        "Check out what happens if you click on the different view elements!",
                        duration = resources.getInteger(R.integer.duration_snackbar_long)
                    )
                    .setIcon(R.drawable.ic_outline_info_24)
                    .build()
                    .show()
            }
    }

    override fun onViewCreatedCore(savedInstanceState: Bundle?) {
        binding.setOnClickListeners()
        viewLifecycleOwner.lifecycle.addObserver(binding.copyrightTv)
    }

    private fun FragmentAboutBinding.setOnClickListeners() {
        appTitleTextView.setOnClickListener { it.animate(Techniques.Wobble) }
        logoIv.setOnClickListener { it.animate(Techniques.Tada) }
        versionTv.setOnClickListener { it.animate(Techniques.RubberBand) }
    }
}