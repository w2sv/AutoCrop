package com.autocrop.activities.main

import com.autocrop.activities.IntentIdentifiers
import com.autocrop.activities.main.fragments.about.AboutFragment
import com.autocrop.activities.main.fragments.flowfield.FlowFieldFragment
import com.autocrop.global.BooleanUserPreferences
import com.autocrop.global.SaveDestinationPreferences
import com.autocrop.uicontroller.activity.FragmentHostingActivity
import com.autocrop.utils.android.*
import com.w2sv.autocrop.databinding.ActivityMainBinding

class MainActivity : FragmentHostingActivity<ActivityMainBinding>() {

    override val rootFragment by lazy{ FlowFieldFragment() }
    val aboutFragment by lazy { AboutFragment() }

    private val nSavedCropsRetriever = IntentExtraRetriever<IntArray>()

    override fun onCreateCore() {
        nSavedCropsRetriever(intent, IntentIdentifiers.N_SAVED_CROPS_WITH_N_DELETED_SCREENSHOTS)?.let {
            val (nSavedCrops, nDeletedScreenshots) = it[0] to it[1]

            displaySnackbar(
                when (nSavedCrops) {
                    0 -> "Discarded all crops"
                    else -> "Saved $nSavedCrops crop${numberInflection(nSavedCrops)}".run {
                        if (nDeletedScreenshots != 0)
                            plus(" and deleted\n${if (nDeletedScreenshots == nSavedCrops) "corresponding" else nDeletedScreenshots} screenshot${numberInflection(nDeletedScreenshots)}")
                        else
                            this
                    }
                },
                TextColors.SUCCESS
            )
        }
    }

    /**
     * Exit app
     */
    override fun onBackPressed() {
        if (aboutFragment.isVisible)
            hideAndShowFragments(aboutFragment, rootFragment)
        else
            finishAffinity()
    }

    /**
     * Write set preferences to shared preferences
     */
    override fun onPause() {
        super.onPause()

        BooleanUserPreferences.writeChangedValuesToSharedPreferences(lazy { getSharedPreferences() })
        SaveDestinationPreferences.writeChangedValuesToSharedPreferences(lazy { getSharedPreferences() })
    }
}