package com.autocrop.activities.main

import android.os.Handler
import android.os.Looper
import android.text.SpannableStringBuilder
import androidx.core.text.color
import com.autocrop.activities.IntentExtraIdentifier
import com.autocrop.activities.main.fragments.about.AboutFragment
import com.autocrop.activities.main.fragments.flowfield.FlowFieldFragment
import com.autocrop.collections.ImageFileIOSynopsis
import com.autocrop.global.preferencesInstances
import com.autocrop.uicontroller.activity.ApplicationActivity
import com.autocrop.utils.numericallyInflected
import com.autocrop.utilsandroid.*
import com.w2sv.autocrop.R

class MainActivity :
    ApplicationActivity<FlowFieldFragment, MainActivityViewModel>(
        FlowFieldFragment::class.java,
        MainActivityViewModel::class.java) {

    /**
     * Notifies as to IO results from previous ExaminationActivity cycle
     */
    override fun triggerEntrySnackbar(){
        intentExtra<ByteArray>(IntentExtraIdentifier.EXAMINATION_ACTIVITY_RESULTS)?.let {
            Handler(Looper.getMainLooper()).postDelayed(
                {
                    ImageFileIOSynopsis(it).run{
                        when (nSavedCrops) {
                            0 -> snacky(
                                "Discarded all crops",
                                R.drawable.ic_outline_sentiment_dissatisfied_24
                            )
                            else -> snacky(
                                SpannableStringBuilder().apply {
                                    append("Saved $nSavedCrops ${"crop".numericallyInflected(nSavedCrops)} to ")
                                    color(getColorInt(NotificationColor.SUCCESS, this@MainActivity)){
                                        append(cropWriteDirIdentifier)
                                    }

                                    if (nDeletedScreenshots != 0)
                                        append(" and deleted ${if (nDeletedScreenshots == nSavedCrops) "corresponding" else nDeletedScreenshots} ${"screenshot".numericallyInflected(nDeletedScreenshots)}")
                                },
                                R.drawable.ic_baseline_done_24
                            )
                        }
                    }
                        .show()
                },
                resources.getInteger(R.integer.fade_in_duration_flowfield_fragment_buttons).toLong() / 2
            )
        }
    }

    /**
     * invoke [FlowFieldFragment] if [AboutFragment] showing, otherwise exit app
     */
    override fun onBackPressed(){
        when (currentFragment()){
            is AboutFragment -> supportFragmentManager.popBackStack()
            else -> finishAffinity()
        }
    }

    /**
     * Write changed values of each [preferencesInstances] element to SharedPreferences
     */
    override fun onStop() {
        super.onStop()

        with(lazy { getApplicationWideSharedPreferences() }){
            preferencesInstances.forEach {
                it.writeChangedValuesToSharedPreferences(this)
            }
        }
    }
}