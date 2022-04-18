package com.autocrop.activities.main

import android.os.Bundle
import android.text.SpannableStringBuilder
import androidx.core.text.color
import com.autocrop.activities.IntentIdentifier
import com.autocrop.activities.main.fragments.about.AboutFragment
import com.autocrop.activities.main.fragments.flowfield.FlowFieldFragment
import com.autocrop.global.userPreferencesInstances
import com.autocrop.uicontroller.activity.FragmentHostingActivity
import com.autocrop.utils.android.*
import com.autocrop.utils.numberInflection
import com.w2sv.autocrop.R
import com.w2sv.autocrop.databinding.ActivityMainBinding

class MainActivity :
    FragmentHostingActivity<ActivityMainBinding>() {

    override val rootFragment by lazy{ FlowFieldFragment() }
    val aboutFragment by lazy { AboutFragment() }

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_App_Main)
        super.onCreate(savedInstanceState)
    }

    /**
     * Return to [FlowFieldFragment] if [AboutFragment] showing, otherwise exit app
     */
    override fun onBackPressed() {
        if (aboutFragment.isVisible)
            hideAndShowFragments(aboutFragment, rootFragment)
        else
            finishAffinity()
    }

    /**
     * Write preferences to disk in case of any changes having been made
     */
    override fun onPause() {
        super.onPause()

        with(lazy { getSharedPreferences() }){
            userPreferencesInstances.forEach {
                it.writeChangedValuesToSharedPreferences(this)
            }
        }
    }
}