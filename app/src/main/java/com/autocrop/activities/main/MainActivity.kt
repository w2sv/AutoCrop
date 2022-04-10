package com.autocrop.activities.main

import com.autocrop.activities.IntentIdentifier
import com.autocrop.activities.main.fragments.about.AboutFragment
import com.autocrop.activities.main.fragments.flowfield.FlowFieldFragment
import com.autocrop.global.userPreferencesInstances
import com.autocrop.uicontroller.activity.FragmentHostingActivity
import com.autocrop.utils.android.*
import com.w2sv.autocrop.databinding.ActivityMainBinding

class MainActivity : FragmentHostingActivity<ActivityMainBinding>(true) {

    override val rootFragment by lazy{ FlowFieldFragment() }
    val aboutFragment by lazy { AboutFragment() }

    private val nSavedCropsRetriever = IntentExtraRetriever<IntArray>(IntentIdentifier.N_SAVED_CROPS_WITH_N_DELETED_SCREENSHOTS)
    private val cropWriteDirPathRetriever = IntentExtraRetriever<String>(IntentIdentifier.CROP_WRITE_DIR_PATH)

    override fun onCreateCore() {
        nSavedCropsRetriever(intent)?.let {
            val (nSavedCrops, nDeletedScreenshots) = it[0] to it[1]

            displaySnackbar(
                when (nSavedCrops) {
                    0 -> "Discarded all crops"
                    else -> "Saved $nSavedCrops crop${numberInflection(nSavedCrops)} to ${cropWriteDirPathRetriever(intent)!!}".run {
                        if (nDeletedScreenshots != 0)
                            plus(" and deleted\n${if (nDeletedScreenshots == nSavedCrops) "corresponding" else nDeletedScreenshots} screenshot${numberInflection(nDeletedScreenshots)}")
                        else
                            this
                    }
                },
                NotificationColor.SUCCESS
            )
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