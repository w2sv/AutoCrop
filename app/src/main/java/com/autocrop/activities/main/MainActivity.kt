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

class MainActivity : FragmentHostingActivity<ActivityMainBinding>() {

    override val rootFragment by lazy{ FlowFieldFragment() }
    val aboutFragment by lazy { AboutFragment() }

    private val nSavedCropsRetriever = IntentExtraRetriever<IntArray>(IntentIdentifier.N_SAVED_CROPS_WITH_N_DELETED_SCREENSHOTS)
    private val cropWriteDirPathRetriever = IntentExtraRetriever<String>(IntentIdentifier.CROP_WRITE_DIR_PATH)

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_App_Main)
        super.onCreate(savedInstanceState)
    }

    override fun onCreateCore() {
        nSavedCropsRetriever(intent)?.let {
            val (nSavedCrops, nDeletedScreenshots) = it[0] to it[1]

            when (nSavedCrops) {
                0 -> displaySnackbar("Discarded all crops", R.drawable.ic_outline_sentiment_dissatisfied_24)
                else ->
                    displaySnackbar(
                        SpannableStringBuilder().apply {
                            append("Saved $nSavedCrops crop${numberInflection(nSavedCrops)} to ")
                            color(getColorInt(NotificationColor.SUCCESS, this@MainActivity)){append(cropWriteDirPathRetriever(intent)!!)}
                            if (nDeletedScreenshots != 0)
                                append("\nand deleted ${if (nDeletedScreenshots == nSavedCrops) "corresponding" else nDeletedScreenshots} screenshot${numberInflection(nDeletedScreenshots)}")
                        },
                        R.drawable.ic_baseline_done_24
                    )
            }
        }
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