/**
 * ViewPager2 Reference: https://medium.com/swlh/android-infinite-auto-image-slider-using-view-pager-2-android-studio-java-a0e450dec071
 */

package com.autocrop.activities.examination

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.get
import com.autocrop.UserPreferences
import com.autocrop.activities.BackPressHandler
import com.autocrop.activities.SystemUiHidingFragmentActivity
import com.autocrop.activities.cropping.N_DISMISSED_IMAGES_IDENTIFIER
import com.autocrop.activities.examination.fragments.DownstreamExaminationActivityFragment
import com.autocrop.activities.examination.fragments.apptitle.AppTitleFragment
import com.autocrop.activities.examination.fragments.examination.ExaminationFragment
import com.autocrop.activities.examination.fragments.saveall.SaveAllFragment
import com.autocrop.activities.main.MainActivity
import com.autocrop.clearAndLog
import com.autocrop.cropBundleList
import com.autocrop.utils.android.*
import com.autocrop.utils.get
import com.autocrop.utils.notNull
import com.google.android.material.snackbar.Snackbar
import com.w2sv.autocrop.R


val N_SAVED_CROPS: String = intentExtraIdentifier("n_saved_crops")


class ExaminationActivity : SystemUiHidingFragmentActivity(R.layout.activity_examination) {
    private lateinit var viewModel: ExaminationViewModel

    private val nDismissedImagesRetriever = IntentExtraRetriever()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // --------retrieve variables
        val nDismissedImages: Int? =
            nDismissedImagesRetriever(intent, N_DISMISSED_IMAGES_IDENTIFIER, 0)
        val conductAutoScroll: Boolean =
            UserPreferences.conductAutoScroll && cropBundleList.size > 1

        viewModel = ViewModelProvider(
            this,
            ExaminationViewModelFactory(
                conductAutoScroll,
                longAutoScrollDelay = nDismissedImages.notNull()
            )
        )[ExaminationViewModel::class.java]

        // --------commit ExaminationFragment if applicable
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .add(
                    R.id.container,
                    ExaminationFragment()
                )
                .commit()
        }

        // ---------display Snackbars
        if (nDismissedImages.notNull())
            displaySnackbar(
                "Couldn't find cropping bounds for\n%d image%s".format(nDismissedImages, listOf("", "s")[nDismissedImages!! > 1]),
                TextColors.urgent
            )
        else if (conductAutoScroll)
            displaySnackbar(
                "Tap screen to cancel auto scrolling",
                TextColors.neutral,
                Snackbar.LENGTH_SHORT
            )
    }

    val saveAllFragment: Lazy<DownstreamExaminationActivityFragment> = lazy { SaveAllFragment() }
    val appTitleFragment: Lazy<DownstreamExaminationActivityFragment> = lazy { AppTitleFragment() }

    /**
     * Block backPress throughout the process of saving all crops,
     * otherwise return to MainActivity after confirmation
     */
    private val backPressHandler = BackPressHandler(this, "Tap again to return to main screen"){
        returnToMainActivity()
    }

    override fun onBackPressed() = when {
        appTitleFragment.isInitialized() -> Unit
        saveAllFragment.isInitialized() -> {
            displaySnackbar(
                "Wait until crops have been saved",
                TextColors.urgent
            )
        }
        else -> backPressHandler()
    }

    /**
     * Clears remaining cropBundle elements contained within cropBundleList
     */
    fun returnToMainActivity() {
        startActivity(
            Intent(
                this,
                MainActivity::class.java
            ).putExtra(N_SAVED_CROPS, viewModel.nSavedCrops)
        )

        returnTransitionAnimation()

        cropBundleList.clearAndLog()
        finishAndRemoveTask()
    }
}