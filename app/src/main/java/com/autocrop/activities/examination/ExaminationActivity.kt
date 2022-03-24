/**
 * ViewPager2 Reference: https://medium.com/swlh/android-infinite-auto-image-slider-using-view-pager-2-android-studio-java-a0e450dec071
 */

package com.autocrop.activities.examination

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.autocrop.UserPreferences
import com.autocrop.activities.BackPressHandler
import com.autocrop.activities.SystemUiHidingFragmentActivity
import com.autocrop.activities.cropping.N_DISMISSED_IMAGES_IDENTIFIER
import com.autocrop.activities.examination.fragments.ExaminationActivityFragment
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
import com.w2sv.autocrop.databinding.ActivityExaminationBinding


val N_SAVED_CROPS: String = intentExtraIdentifier("n_saved_crops")


class ExaminationActivity : SystemUiHidingFragmentActivity() {
    private lateinit var viewModel: ExaminationViewModel

    private val nDismissedImagesRetriever = IntentExtraRetriever()

    private lateinit var binding: ActivityExaminationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityExaminationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // --------retrieve variables
        val nDismissedImages: Int? =
            nDismissedImagesRetriever(intent, N_DISMISSED_IMAGES_IDENTIFIER, 0)
        val conductAutoScroll: Boolean =
            UserPreferences.conductAutoScroll && cropBundleList.size > 1

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
                .add(binding.container.id, ExaminationFragment())
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

    val saveAllFragment: Lazy<Fragment> = lazy { SaveAllFragment() }
    val appTitleFragment: Lazy<Fragment> = lazy { AppTitleFragment() }

    fun invoke(fragment: Lazy<Fragment>, flipRight: Boolean) {
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
            // .setCustomAnimations(animations[0], animations[1])
            .add(binding.container.id, fragment.value)
//            .addToBackStack(null)
//            .setReorderingAllowed(true)
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
            ).putExtra(N_SAVED_CROPS, viewModel.nSavedCrops)
        )

        returnTransitionAnimation()

        cropBundleList.clearAndLog()
        finishAndRemoveTask()
    }
}