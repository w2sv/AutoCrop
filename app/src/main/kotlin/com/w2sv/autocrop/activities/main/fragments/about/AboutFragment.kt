package com.w2sv.autocrop.activities.main.fragments.about

import android.os.Bundle
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import com.daimajia.androidanimations.library.Techniques
import com.w2sv.androidutils.ui.SimpleAnimationListener
import com.w2sv.autocrop.R
import com.w2sv.autocrop.activities.AppFragment
import com.w2sv.autocrop.databinding.FragmentAboutBinding
import com.w2sv.autocrop.preferences.GlobalFlags
import com.w2sv.autocrop.ui.animate
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AboutFragment :
    AppFragment<FragmentAboutBinding>(FragmentAboutBinding::class.java) {

    @Inject
    lateinit var globalFlags: GlobalFlags

    override fun onCreateAnimation(transit: Int, enter: Boolean, nextAnim: Int): Animation? =
        if (enter)
            AnimationUtils.loadAnimation(requireActivity(), nextAnim)
                .apply {
                    setAnimationListener(
                        object : SimpleAnimationListener() {
                            override fun onAnimationEnd(animation: Animation?) {
                                super.onAnimationEnd(animation)

                                showInstructionSnackbarIfApplicable()
                            }
                        }
                    )
                }
        else
            super.onCreateAnimation(transit, false, nextAnim)

    private fun showInstructionSnackbarIfApplicable() {
        if (!globalFlags.aboutFragmentInstructionsShown)
            getSnackyBuilder(
                "Check out what happens if you click on the different view elements!",
                duration = resources.getInteger(R.integer.duration_snackbar_long)
            )
                .setIcon(R.drawable.ic_info_24)
                .build()
                .show()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.setOnClickListeners()
    }

    private fun FragmentAboutBinding.setOnClickListeners() {
        appTitleTextView.setOnClickListener { it.animate(Techniques.Wobble) }
        logoIv.setOnClickListener { it.animate(Techniques.Tada) }
        versionTv.setOnClickListener { it.animate(Techniques.RubberBand) }
    }
}