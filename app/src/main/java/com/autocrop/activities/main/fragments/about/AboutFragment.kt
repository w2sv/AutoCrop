package com.autocrop.activities.main.fragments.about

import android.content.Context
import android.util.AttributeSet
import com.autocrop.activities.main.fragments.MainActivityFragment
import com.autocrop.utils.android.ExtendedTextView
import com.w2sv.autocrop.BuildConfig
import com.w2sv.autocrop.R
import com.w2sv.autocrop.databinding.ActivityMainFragmentAboutBinding
import java.util.*

class AboutFragment: MainActivityFragment<ActivityMainFragmentAboutBinding>()

class VersionTextView(context: Context, attr: AttributeSet): ExtendedTextView(context, attr, R.string.version){
    init { text = getString().format(BuildConfig.VERSION_NAME) }
}

class CopyrightTextView(context: Context, attr: AttributeSet): ExtendedTextView(context, attr, R.string.copyright){
    init { text = getString().format(Calendar.getInstance().get(Calendar.YEAR)) }
}