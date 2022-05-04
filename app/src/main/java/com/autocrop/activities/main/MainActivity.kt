package com.autocrop.activities.main

import android.text.SpannableStringBuilder
import androidx.core.text.color
import com.autocrop.activities.IntentExtraIdentifier
import com.autocrop.activities.main.fragments.about.AboutFragment
import com.autocrop.activities.main.fragments.flowfield.FlowFieldFragment
import com.autocrop.global.userPreferencesInstances
import com.autocrop.uicontroller.activity.ApplicationActivity
import com.autocrop.utils.android.*
import com.autocrop.utils.numericallyInflected
import com.w2sv.autocrop.R
import com.w2sv.autocrop.databinding.MainBinding

class MainActivity :
    ApplicationActivity<MainBinding, FlowFieldFragment, MainActivityViewModel>(
        FlowFieldFragment::class.java,
        MainActivityViewModel::class.java) {

    /**
     * Notifies as to IO results from previous ExaminationActivity cycle
     */
    override fun displayEntrySnackbar(){
        intentExtra<IntArray>(IntentExtraIdentifier.N_SAVED_CROPS_WITH_N_DELETED_SCREENSHOTS)?.let {
            val (nSavedCrops, nDeletedScreenshots) = it[0] to it[1]

            when (nSavedCrops) {
                0 -> snacky(
                    "Discarded all crops",
                    R.drawable.ic_outline_sentiment_dissatisfied_24
                )
                else -> snacky(
                    SpannableStringBuilder().apply {
                        append("Saved $nSavedCrops ${"crop".numericallyInflected(nSavedCrops)} to ")
                        color(getColorInt(NotificationColor.SUCCESS, this@MainActivity)){append(intentExtra<String>(IntentExtraIdentifier.CROP_WRITE_DIR_PATH)!!)}
                        if (nDeletedScreenshots != 0)
                            append(" and deleted ${if (nDeletedScreenshots == nSavedCrops) "corresponding" else nDeletedScreenshots} ${"screenshot".numericallyInflected(nDeletedScreenshots)}")
                    },
                    R.drawable.ic_baseline_done_24
                )
            }
                .show()
        }
    }

    /**
     * invoke [FlowFieldFragment] if [AboutFragment] showing, otherwise exit app
     */
    override fun onBackPressed(){
        if (currentFragment() is AboutFragment)
            replaceCurrentFragmentWith(FlowFieldFragment(), false)
        else
            finishAffinity()
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