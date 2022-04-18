package com.autocrop.activities.main.fragments.about

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.AttributeSet
import android.view.View
import com.autocrop.activities.main.fragments.MainActivityFragment
import com.autocrop.uielements.ExtendedTextView
import com.autocrop.uielements.setAnimation
import com.daimajia.androidanimations.library.Techniques
import com.w2sv.autocrop.BuildConfig
import com.w2sv.autocrop.R
import com.w2sv.autocrop.databinding.ActivityMainFragmentAboutBinding
import java.util.*

class AboutFragment: MainActivityFragment<ActivityMainFragmentAboutBinding>(){
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.appTitleTextView.setOnClickListener { it.setAnimation(Techniques.Wobble) }
        binding.trippyBrudinelettenImageView.setOnClickListener { it.setAnimation(Techniques.Tada) }

        binding.w2svTv.setOnClickListener {
            val viewIntent = Intent(
                "android.intent.action.VIEW",
                Uri.parse("http://github.com/w2sv")
            )
            startActivity(viewIntent)
        }
    }
}

class VersionTextView(context: Context, attr: AttributeSet): ExtendedTextView(context, attr, R.string.version){
    init { text = getString().format(BuildConfig.VERSION_NAME) }
}

class CopyrightTextView(context: Context, attr: AttributeSet): ExtendedTextView(context, attr, R.string.copyright){
    init { text = getString().format(Calendar.getInstance().get(Calendar.YEAR)) }
}