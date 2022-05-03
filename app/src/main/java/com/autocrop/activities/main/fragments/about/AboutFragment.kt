package com.autocrop.activities.main.fragments.about

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import com.autocrop.activities.main.fragments.MainActivityFragment
import com.autocrop.uielements.view.animate
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo
import com.w2sv.autocrop.databinding.MainFragmentAboutBinding

class AboutFragment:
    MainActivityFragment<MainFragmentAboutBinding>(){

    private var w2svTvAnimation: YoYo.YoYoString? = null

    override fun onViewCreatedCore(savedInstanceState: Bundle?) {
        binding.appTitleTextView.setOnClickListener { it.animate(Techniques.Wobble) }
        binding.trippyBrudinelettenImageView.setOnClickListener { it.animate(Techniques.Tada) }

        binding.w2svTv.setOnClickListener {
            w2svTvAnimation = it.animate(Techniques.ZoomOutUp)

            startActivity(
                Intent(
                    "android.intent.action.VIEW",
                    Uri.parse("http://github.com/w2sv")
                )
            )
        }
    }

    override fun onStop() {
        super.onStop()

        w2svTvAnimation?.stop(true)
    }

    override fun onResume() {
        super.onResume()

        w2svTvAnimation = null
    }
}