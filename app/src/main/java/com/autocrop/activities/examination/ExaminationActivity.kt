/**
 * ViewPager2 Reference: https://medium.com/swlh/android-infinite-auto-image-slider-using-view-pager-2-android-studio-java-a0e450dec071
 */

package com.autocrop.activities.examination

import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import com.autocrop.UserPreferences
import com.autocrop.activities.BackPressHandler
import com.autocrop.activities.IntentIdentifiers
import com.autocrop.activities.SystemUiHidingFragmentActivity
import com.autocrop.activities.examination.fragments.singleaction.SingleActionExaminationActivityFragment
import com.autocrop.activities.examination.fragments.singleaction.apptitle.AppTitleFragment
import com.autocrop.activities.examination.fragments.singleaction.saveall.SaveAllFragment
import com.autocrop.activities.examination.fragments.viewpager.ViewPagerFragment
import com.autocrop.activities.main.MainActivity
import com.autocrop.clearAndLog
import com.autocrop.cropBundleList
import com.autocrop.utils.android.IntentExtraRetriever
import com.autocrop.utils.android.TextColors
import com.autocrop.utils.android.displaySnackbar
import com.autocrop.utils.android.returnTransitionAnimation
import com.autocrop.utils.get
import com.autocrop.utils.notNull
import com.google.android.material.snackbar.Snackbar
import com.w2sv.autocrop.R
import com.w2sv.autocrop.databinding.ActivityExaminationBinding

class ExaminationActivity : SystemUiHidingFragmentActivity() {
    private lateinit var viewModel: ExaminationViewModel

    private val nDismissedImagesRetriever = IntentExtraRetriever()

    private lateinit var binding: ActivityExaminationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ----------retrieve ViewBinding
        binding = ActivityExaminationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // --------retrieve variables
        val nDismissedImages: Int? =
            nDismissedImagesRetriever(intent, IntentIdentifiers.N_DISMISSED_IMAGES, 0)
        val conductAutoScroll: Boolean =
            UserPreferences.conductAutoScrolling && cropBundleList.size > 1

        //----------retrieve ViewModel
        viewModel = ViewModelProvider(
            this,
            ExaminationViewModelFactory(
                conductAutoScroll,
                longAutoScrollDelay = nDismissedImages.notNull()
            )
        )[ExaminationViewModel::class.java]

        // --------commit ExaminationFragment if applicable
        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .add(binding.container.id, ViewPagerFragment())
                .commit()
        }

        // ---------display Snackbars
        if (nDismissedImages.notNull())
            displaySnackbar(
                "Couldn't find cropping bounds for\n$nDismissedImages image${listOf("", "s")[nDismissedImages!! > 1]}",
                TextColors.urgent
            )
        else if (conductAutoScroll)
            displaySnackbar(
                "Tap screen to cancel auto scrolling",
                TextColors.neutral,
                Snackbar.LENGTH_SHORT
            )
    }

    val saveAllFragment: Lazy<SaveAllFragment> = lazy { SaveAllFragment() }
    val appTitleFragment: Lazy<AppTitleFragment> = lazy { AppTitleFragment() }

    fun Lazy<SingleActionExaminationActivityFragment>.commit(flipRight: Boolean){
        val animations = arrayOf(
            arrayOf(
                R.animator.card_flip_left_in,
                R.animator.card_flip_left_out
            ),
            arrayOf(
                R.animator.card_flip_right_in,
                R.animator.card_flip_right_out
            )
        )[flipRight]

        supportFragmentManager
            .beginTransaction()
            .setCustomAnimations(animations[0], animations[1])
            .replace(binding.container.id, value)
            .addToBackStack(null)
            .setReorderingAllowed(true)
            .commit()
    }

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
            ).putExtra(IntentIdentifiers.N_SAVED_CROPS, viewModel.nSavedCrops)
        )

        returnTransitionAnimation()

        viewModel.viewPager.dataSet.clearAndLog()
        finishAndRemoveTask()
    }
}