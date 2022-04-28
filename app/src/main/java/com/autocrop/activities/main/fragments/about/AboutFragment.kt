package com.autocrop.activities.main.fragments.about

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.AttributeSet
import android.view.View
import com.autocrop.activities.main.fragments.MainActivityFragment
import com.autocrop.uielements.view.StringResourceCoupledTextView
import com.autocrop.uielements.view.animate
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo
import com.w2sv.autocrop.BuildConfig
import com.w2sv.autocrop.R
import com.w2sv.autocrop.databinding.MainFragmentAboutBinding
import java.util.*

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

class VersionTextView(context: Context, attr: AttributeSet):
    StringResourceCoupledTextView(context, attr, R.string.version){

    init {
        text = stringResource.format(BuildConfig.VERSION_NAME)

        setOnClickListener { it.animate(Techniques.RubberBand) }
    }
}

class CopyrightTextView(context: Context, attr: AttributeSet):
    StringResourceCoupledTextView(context, attr, R.string.copyright){

    init { text = stringResource.format(Calendar.getInstance().get(Calendar.YEAR)) }
}