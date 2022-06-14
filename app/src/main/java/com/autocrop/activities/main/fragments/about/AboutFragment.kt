package com.autocrop.activities.main.fragments.about

import android.animation.Animator
import android.animation.AnimatorInflater
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.autocrop.activities.main.fragments.MainActivityFragment
import com.autocrop.preferences.BooleanPreferences
import com.autocrop.uielements.view.animate
import com.autocrop.utilsandroid.SimpleAnimatorListener
import com.autocrop.utilsandroid.buildAndShow
import com.autocrop.utilsandroid.snacky
import com.daimajia.androidanimations.library.Techniques
import com.w2sv.autocrop.R
import com.w2sv.autocrop.databinding.MainFragmentAboutBinding

class AboutFragment:
    MainActivityFragment<MainFragmentAboutBinding>(MainFragmentAboutBinding::class.java){

    override fun onCreateAnimator(transit: Int, enter: Boolean, nextAnim: Int): Animator? =
        if (enter && !BooleanPreferences.aboutFragmentInstructionsShown)
            AnimatorInflater.loadAnimator(activity, nextAnim)
                .apply {
                    addListener(object : SimpleAnimatorListener() {
                        override fun onAnimationEnd(animation: Animator?) {
                            Handler(Looper.getMainLooper()).postDelayed(
                                {
                                    requireActivity()
                                        .snacky(
                                            "Pro tip: check out what happens if you click on the various view elements",
                                            duration = resources.getInteger(R.integer.duration_snackbar_long)
                                        )
                                        .setIcon(R.drawable.ic_outline_info_24)
                                        .buildAndShow()
                                },
                                resources.getInteger(R.integer.delay_minimal).toLong()
                            )
                        }
                    })
                }
        else
            super.onCreateAnimator(transit, enter, nextAnim)

    override fun onViewCreatedCore(savedInstanceState: Bundle?) {
        binding.setOnClickListeners()

        listOf(
            binding.copyrightTv,
            binding.w2svTv
        ).forEach {
            viewLifecycleOwner.lifecycle.addObserver(it)
        }
    }

    private fun MainFragmentAboutBinding.setOnClickListeners(){
        appTitleTextView.setOnClickListener { it.animate(Techniques.Wobble) }
        logoIv.setOnClickListener { it.animate(Techniques.Tada) }
    }
}