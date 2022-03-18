/**
 * ViewPager2 Reference: https://medium.com/swlh/android-infinite-auto-image-slider-using-view-pager-2-android-studio-java-a0e450dec071
 */

package com.autocrop.activities.examination

import android.content.Intent
import android.os.Bundle
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
import com.w2sv.autocrop.R
import com.google.android.material.snackbar.Snackbar


val N_SAVED_CROPS: String = intentExtraIdentifier("n_saved_crops")


class ExaminationActivity : SystemUiHidingFragmentActivity(R.layout.activity_examination) {
    var nSavedCrops: Int = 0

    val nDismissedImagesRetriever = IntentExtraRetriever()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // --------retrieve variables
        val nDismissedImages: Int? =
            nDismissedImagesRetriever(intent, N_DISMISSED_IMAGES_IDENTIFIER, 0)
        val conductAutoScroll: Boolean =
            UserPreferences.conductAutoScroll && cropBundleList.size > 1

        // --------commit ExaminationFragment if applicable
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .add(
                    R.id.container,
                    ExaminationFragment(
                        conductAutoScroll,
                        longAutoScrollDelay = nDismissedImages.notNull()
                    )
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

    private val saveAllFragment: Lazy<ExaminationActivityFragment> = lazy { SaveAllFragment() }
    private val appTitleFragment: Lazy<ExaminationActivityFragment> = lazy { AppTitleFragment() }

    fun invokeSaveAllFragment() {
        invokeFragment(
            saveAllFragment,
            flipRightAnimations
        )
    }

    fun invokeAppTitleFragment(flipRight: Boolean) {
        invokeFragment(
            appTitleFragment,
            listOf(
                arrayOf(
                    R.animator.card_flip_left_in,
                    R.animator.card_flip_left_out
                ),
                flipRightAnimations
            )[flipRight]
        )
    }

    private val flipRightAnimations: Array<Int> = arrayOf(
        R.animator.card_flip_right_in,
        R.animator.card_flip_right_out
    )

    private fun invokeFragment(
        lazyFragment: Lazy<ExaminationActivityFragment>,
        animations: Array<Int>
    ) {
        supportFragmentManager
            .beginTransaction()
            .setCustomAnimations(
                animations[0],
                animations[1]
            )
            .replace(R.id.container, lazyFragment.value)
            .addToBackStack(null)
            .commit()
    }

    /**
     * Blocked throughout the process of saving all crops,
     * otherwise asks for confirmation through second back press;
     *
     * Results in return to main activity
     */
    private val backPressHandler = BackPressHandler()

    override fun onBackPressed() {
        when {
            appTitleFragment.isInitialized() -> Unit
            saveAllFragment.isInitialized() -> {
                displaySnackbar(
                    "Wait until crops have been saved",
                    TextColors.urgent
                )
            }
            backPressHandler.pressedOnce -> returnToMainActivity()
            else -> {
                backPressHandler.onPress()
                displaySnackbar(
                    "Tap again to return to main screen",
                    TextColors.neutral
                )
            }
        }
    }

    /**
     * Clears remaining cropBundle elements contained within cropBundleList
     */
    fun returnToMainActivity() {
        startActivity(
            Intent(
                this,
                MainActivity::class.java
            ).putExtra(N_SAVED_CROPS, nSavedCrops)
        )

        returnTransitionAnimation()

        cropBundleList.clearAndLog()
        finishAndRemoveTask()
    }
}