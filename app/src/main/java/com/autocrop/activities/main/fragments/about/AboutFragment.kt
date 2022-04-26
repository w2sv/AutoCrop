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
import com.w2sv.autocrop.BuildConfig
import com.w2sv.autocrop.R
import com.w2sv.autocrop.databinding.ActivityMainFragmentAboutBinding
import java.util.*

class AboutFragment: MainActivityFragment<ActivityMainFragmentAboutBinding>(){
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.appTitleTextView.setOnClickListener { it.animate(Techniques.Wobble) }
        binding.trippyBrudinelettenImageView.setOnClickListener { it.animate(Techniques.Tada) }

        binding.w2svTv.setOnClickListener {
            // WHOOPSIE DAISY! - view simply disappears after ZoomOutRight
            // and has to be thus ZoomInRight'd back in
            it.animate(Techniques.ZoomOutRight){
                it.animate(Techniques.ZoomInRight)
            }

            startActivity(
                Intent(
                    "android.intent.action.VIEW",
                    Uri.parse("http://github.com/w2sv")
                )
            )
        }
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