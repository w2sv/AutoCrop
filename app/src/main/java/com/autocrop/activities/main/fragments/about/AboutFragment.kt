package com.autocrop.activities.main.fragments.about

import android.content.Context
import android.os.Bundle
import android.util.AttributeSet
import android.view.View
import com.autocrop.activities.main.MainActivity
import com.autocrop.activities.main.fragments.MainActivityFragment
import com.autocrop.uicontroller.activity.ViewBindingHandlingActivity
import com.autocrop.uicontroller.fragment.ViewBindingHoldingFragment
import com.autocrop.uielements.ExtendedTextView
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo
import com.w2sv.autocrop.BuildConfig
import com.w2sv.autocrop.R
import com.w2sv.autocrop.databinding.ActivityMainFragmentAboutBinding
import java.util.*

class AboutFragment: MainActivityFragment<ActivityMainFragmentAboutBinding>(){
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.appTitleTextView.setOnClickListener {
            YoYo.with(Techniques.Wobble)
                .duration(1000)
                .playOn(it)
        }
    }
}

class VersionTextView(context: Context, attr: AttributeSet): ExtendedTextView(context, attr, R.string.version){
    init { text = getString().format(BuildConfig.VERSION_NAME) }
}

class CopyrightTextView(context: Context, attr: AttributeSet): ExtendedTextView(context, attr, R.string.copyright){
    init { text = getString().format(Calendar.getInstance().get(Calendar.YEAR)) }
}