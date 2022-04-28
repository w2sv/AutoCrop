package com.autocrop.activities.main

import androidx.activity.viewModels
import androidx.fragment.app.Fragment
import com.autocrop.activities.main.fragments.about.AboutFragment
import com.autocrop.activities.main.fragments.flowfield.FlowFieldFragment
import com.autocrop.global.userPreferencesInstances
import com.autocrop.uicontroller.activity.FragmentHostingActivity
import com.autocrop.utils.android.getApplicationWideSharedPreferences
import com.w2sv.autocrop.databinding.MainBinding

class MainActivity :
    FragmentHostingActivity<MainBinding>() {

    override val rootFragment by lazy{ FlowFieldFragment() }

    private val sharedViewModel: MainActivityViewModel by viewModels()

    /**
     * Return to [FlowFieldFragment] if [AboutFragment] showing, otherwise exit app
     */
    override fun onBackPressed() {
        supportFragmentManager.findFragmentById(binding.root.id)?.let { fragment ->
            if (fragment is AboutFragment)
                returnToRootFragment(fragment)
            else
                finishAffinity()
        }
    }

    private fun returnToRootFragment(attachedRootFragment: Fragment){
        if (sharedViewModel.reinitializeRootFragment){
            replaceCurrentFragmentWith(
                FlowFieldFragment(),
                leftFlipAnimationIds
            )
            sharedViewModel.resetValues()
        }
        else
            swapFragments(attachedRootFragment, rootFragment)
    }

    /**
     * Write changed values of each [userPreferencesInstances] element to SharedPreferences
     */
    override fun onStop() {
        super.onStop()

        with(lazy { getApplicationWideSharedPreferences() }){
            userPreferencesInstances.forEach {
                it.writeChangedValuesToSharedPreferences(this)
            }
        }
    }
}