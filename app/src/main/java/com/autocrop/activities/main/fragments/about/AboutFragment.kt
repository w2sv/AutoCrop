package com.autocrop.activities.main.fragments.about

import android.animation.Animator
import android.animation.AnimatorInflater
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.autocrop.activities.main.fragments.MainActivityFragment
import com.autocrop.preferences.BooleanPreferences
import com.autocrop.utils.android.extensions.animate
import com.autocrop.utils.android.extensions.show
import com.autocrop.utils.android.extensions.snacky
import com.daimajia.androidanimations.library.Techniques
import com.w2sv.autocrop.R
import com.w2sv.autocrop.databinding.FragmentAboutBinding

class AboutFragment:
    MainActivityFragment<FragmentAboutBinding>(FragmentAboutBinding::class.java){

    override fun onCreateAnimator(transit: Int, enter: Boolean, nextAnim: Int): Animator? =
        if (enter && !BooleanPreferences.aboutFragmentInstructionsShown)
            AnimatorInflater.loadAnimator(activity, nextAnim)
                .apply {
                    addListener(object : Animator.AnimatorListener {
                        override fun onAnimationStart(animation: Animator) {}
                        override fun onAnimationEnd(animation: Animator) {
                            Handler(Looper.getMainLooper()).postDelayed(
                                {
                                    requireActivity()
                                        .snacky(
                                            "Pro tip: check out what happens if you click on the various view elements",
                                            duration = resources.getInteger(R.integer.duration_snackbar_long)
                                        )
                                        .setIcon(R.drawable.ic_outline_info_24)
                                        .show()
                                },
                                resources.getInteger(R.integer.delay_minimal).toLong()
                            )
                        }
                        override fun onAnimationCancel(animation: Animator) {}
                        override fun onAnimationRepeat(animation: Animator) {}
                    })
                }
        else
            super.onCreateAnimator(transit, enter, nextAnim)

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