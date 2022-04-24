package com.autocrop.activities.main

import android.os.Bundle
import com.autocrop.activities.main.fragments.about.AboutFragment
import com.autocrop.activities.main.fragments.flowfield.FlowFieldFragment
import com.autocrop.global.userPreferencesInstances
import com.autocrop.uicontroller.activity.FragmentHostingActivity
import com.autocrop.utils.android.getApplicationWideSharedPreferences
import com.w2sv.autocrop.R
import com.w2sv.autocrop.databinding.ActivityMainBinding

class MainActivity :
    FragmentHostingActivity<ActivityMainBinding>() {

    override val rootFragment by lazy{ FlowFieldFragment() }
    val aboutFragment by lazy { AboutFragment() }

    override fun onCreate(savedInstanceState: Bundle?) {
        if (savedInstanceState == null)
            setTheme(R.style.Theme_App_Main)

        super.onCreate(savedInstanceState)
    }

    /**
     * Return to [FlowFieldFragment] if [AboutFragment] showing, otherwise exit app
     */
    override fun onBackPressed() {
        if (aboutFragment.isVisible)
            swapFragments(aboutFragment, rootFragment)
        else
            finishAffinity()
    }

    override fun onStop() {
        super.onStop()

        with(lazy { getApplicationWideSharedPreferences() }){
            userPreferencesInstances.forEach {
                it.writeChangedValuesToSharedPreferences(this)
            }
        }
    }
}