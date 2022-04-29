package com.autocrop.activities.main

import android.text.SpannableStringBuilder
import androidx.activity.viewModels
import androidx.core.text.color
import androidx.fragment.app.Fragment
import com.autocrop.activities.IntentExtraIdentifier
import com.autocrop.activities.main.fragments.about.AboutFragment
import com.autocrop.activities.main.fragments.flowfield.FlowFieldFragment
import com.autocrop.global.userPreferencesInstances
import com.autocrop.uicontroller.activity.FragmentHostingActivity
import com.autocrop.utils.android.*
import com.autocrop.utils.numericallyInflected
import com.w2sv.autocrop.R
import com.w2sv.autocrop.databinding.MainBinding

class MainActivity :
    FragmentHostingActivity<MainBinding, FlowFieldFragment>(FlowFieldFragment::class.java) {

    private val sharedViewModel: MainActivityViewModel by viewModels()

    /**
     * Notifies as to IO results from previous ExaminationActivity cycle
     */
    override fun displayEntrySnackbar(){
        intentExtra<IntArray>(IntentExtraIdentifier.N_SAVED_CROPS_WITH_N_DELETED_SCREENSHOTS)?.let {
            val (nSavedCrops, nDeletedScreenshots) = it[0] to it[1]

            when (nSavedCrops) {
                0 -> displaySnackbar("Discarded all crops", R.drawable.ic_outline_sentiment_dissatisfied_24)
                else ->
                    displaySnackbar(
                        SpannableStringBuilder().apply {
                            append("Saved $nSavedCrops ${"crop".numericallyInflected(nSavedCrops)} to ")
                            color(getColorInt(NotificationColor.SUCCESS, this@MainActivity)){append(intentExtra<String>(IntentExtraIdentifier.CROP_WRITE_DIR_PATH)!!)}
                            if (nDeletedScreenshots != 0)
                                append(" and deleted ${if (nDeletedScreenshots == nSavedCrops) "corresponding" else nDeletedScreenshots} ${"screenshot".numericallyInflected(nDeletedScreenshots)}")
                        },
                        R.drawable.ic_baseline_done_24
                    )
            }
        }
    }

    /**
     * [returnToFlowFieldFragment] if [AboutFragment] showing, otherwise exit app
     */
    override fun onBackPressed() {
        currentFragment()?.let { fragment ->
            if (fragment is AboutFragment)
                returnToFlowFieldFragment(fragment)
            else
                finishAffinity()
        }
    }

    private fun returnToFlowFieldFragment(attachedFragment: Fragment){
        if (sharedViewModel.reinitializeRootFragment){
            replaceCurrentFragmentWith(
                FlowFieldFragment(),
                false
            )
            sharedViewModel.resetValues()
        }
        else
            swapFragments(attachedFragment, rootFragment()!!)
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