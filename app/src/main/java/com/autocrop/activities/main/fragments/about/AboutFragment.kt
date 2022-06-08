package com.autocrop.activities.main.fragments.about

import android.animation.Animator
import android.animation.AnimatorInflater
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.autocrop.activities.main.fragments.MainActivityFragment
import com.autocrop.global.BooleanPreferences
import com.autocrop.uielements.view.animate
import com.autocrop.utilsandroid.buildAndShow
import com.autocrop.utilsandroid.snacky
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo
import com.w2sv.autocrop.R
import com.w2sv.autocrop.databinding.MainFragmentAboutBinding

class AboutFragment:
    MainActivityFragment<MainFragmentAboutBinding>(MainFragmentAboutBinding::class.java){

    private var w2svTvAnimation: YoYo.YoYoString? = null
    private var copyrightTvAnimation: YoYo.YoYoString? = null

    override fun onCreateAnimator(transit: Int, enter: Boolean, nextAnim: Int): Animator? =
        if (enter && !BooleanPreferences.aboutFragmentInstructionsShown)
            AnimatorInflater.loadAnimator(activity, nextAnim)
                .apply {
                    addListener(object : Animator.AnimatorListener {
                        override fun onAnimationStart(animation: Animator?) {}
                        override fun onAnimationEnd(animation: Animator?) {
                            Handler(Looper.getMainLooper()).postDelayed(
                                {
                                    requireActivity()
                                        .snacky("Pro tip: check out what happens if you click on the various view elements")
                                        .setIcon(R.drawable.ic_outline_info_24)
                                        .setDuration(4000)
                                        .buildAndShow()
                                    BooleanPreferences.aboutFragmentInstructionsShown = true
                                },
                                resources.getInteger(R.integer.delay_minimal).toLong()
                            )
                        }
                        override fun onAnimationCancel(animation: Animator?) {}
                        override fun onAnimationRepeat(animation: Animator?) {}
                    })
                }
        else
            super.onCreateAnimator(transit, enter, nextAnim)

    override fun onViewCreatedCore(savedInstanceState: Bundle?) {
        binding.setOnClickListeners()
    }

    private fun MainFragmentAboutBinding.setOnClickListeners(){
        appTitleTextView.setOnClickListener { it.animate(Techniques.Wobble) }
        logoIv.setOnClickListener { it.animate(Techniques.Tada) }
        w2svTv.setOnClickListener {
            w2svTvAnimation = it.animate(Techniques.ZoomOutUp){
                startActivity(
                    Intent(
                        "android.intent.action.VIEW",
                        Uri.parse("http://github.com/w2sv")
                    )
                )
            }
        }
        copyrightTv.setOnClickListener{
            copyrightTvAnimation = it.animate(Techniques.ZoomOutRight){
                startActivity(
                    Intent(
                        "android.intent.action.VIEW",
                        Uri.parse("https://github.com/w2sv/AutoCrop/blob/master/LICENSE")
                    )
                )
            }
        }
    }

    override fun onStop() {
        super.onStop()

        w2svTvAnimation?.stop(true)
        copyrightTvAnimation?.stop(true)
    }

    override fun onResume() {
        super.onResume()

        w2svTvAnimation = null
        copyrightTvAnimation = null
    }
}