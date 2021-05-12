/**
 * ViewPager2 Reference: https://medium.com/swlh/android-infinite-auto-image-slider-using-view-pager-2-android-studio-java-a0e450dec071
 */

package com.autocrop.activities.examination

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.autocrop.activities.BackPressHandler
import com.autocrop.activities.SystemUiHidingFragmentActivity
import com.autocrop.activities.cropping.N_DISMISSED_IMAGES_IDENTIFIER
import com.autocrop.activities.examination.cardfragments.back.CardBackFragment
import com.autocrop.activities.examination.cardfragments.front.CardFrontFragment
import com.autocrop.activities.main.MainActivity
import com.autocrop.clearCropBundleList
import com.autocrop.cropBundleList
import com.autocrop.retentionPercentage
import com.autocrop.utils.android.*
import com.bunsenbrenner.screenshotboundremoval.R
import kotlinx.android.synthetic.main.toolbar_examination_activity.*


val N_SAVED_CROPS: String = intentExtraIdentifier("n_saved_crops")


class ExaminationActivity : SystemUiHidingFragmentActivity() {
    var nSavedCrops: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        fun displayCropDismissalToast(nDismissedImages: Int) {
            with(R.color.magenta) {
                when (nDismissedImages) {
                    1 -> displaySnackbar("Couldn't find cropping bounds for\n1 image", this)
                    in 2..Int.MAX_VALUE -> displaySnackbar(
                        "Couldn't find cropping bounds for\n$nDismissedImages images",
                        this
                    )
                }
            }
        }

        setContentView(R.layout.activity_examination)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .add(R.id.container, CardFrontFragment())
                .commit()
        }

        snackbarArgument(displayedSnackbar, N_DISMISSED_IMAGES_IDENTIFIER, 0)?.let {
            displayCropDismissalToast(it)
            displayedSnackbar = true
        }
    }

    fun invokeBackCard(displaySaveAllScreen: Boolean) {
        cardBackFragment = CardBackFragment(displaySaveAllScreen)

        supportFragmentManager
            .beginTransaction()
            .setCustomAnimations(
                R.animator.card_flip_right_in,
                R.animator.card_flip_right_out,
                R.animator.card_flip_left_in,
                R.animator.card_flip_left_out
            )
            .replace(R.id.container, cardBackFragment)
            .addToBackStack(null)
            .commit()
    }

    private lateinit var cardBackFragment: CardBackFragment
    private var displayedSnackbar: Boolean = false

    private val backPressHandler = BackPressHandler()

    /**
     * Blocked throughout the process of saving all crops,
     * otherwise asks for second one as confirmation;
     *
     * Results in return to main activity
     */
    override fun onBackPressed() {

        // block if saving all / dismissing all
        if (cardBackFragment.isVisible) {
            if (cardBackFragment.displayingSaveAllScreen)
                displayToast("Please wait until crops\nhave been saved")
            return
        }

        // return to main activity if already pressed once
        else if (backPressHandler.pressedOnce) {
            return returnToMainActivity()
        }

        backPressHandler.onPress()
        displayToast("Tap again to return to main screen")
    }

    /**
     * Clears remaining cropBundle elements contained within cropBundleList
     */
    fun returnToMainActivity() {
        return startActivity(
            Intent(
                this,
                MainActivity::class.java
            ).putExtra(N_SAVED_CROPS, nSavedCrops)
        ).also {
            if (cardBackFragment.displayingSaveAllScreen)
                restartTransitionAnimation()
            else
                proceedTransitionAnimation()
            onExit()
        }
    }

    private fun onExit() {
        clearCropBundleList()
        finishAndRemoveTask()
    }
}