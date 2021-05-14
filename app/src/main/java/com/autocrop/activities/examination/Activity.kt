/**
 * ViewPager2 Reference: https://medium.com/swlh/android-infinite-auto-image-slider-using-view-pager-2-android-studio-java-a0e450dec071
 */

package com.autocrop.activities.examination

import android.content.Intent
import android.os.Bundle
import com.autocrop.UserPreferences
import com.autocrop.activities.BackPressHandler
import com.autocrop.activities.InterstitialAdWrapper
import com.autocrop.activities.SystemUiHidingFragmentActivity
import com.autocrop.activities.cropping.N_DISMISSED_IMAGES_IDENTIFIER
import com.autocrop.activities.examination.fragments.ExaminationActivityFragment
import com.autocrop.activities.examination.fragments.apptitle.AppTitleFragment
import com.autocrop.activities.examination.fragments.examination.ExaminationFragment
import com.autocrop.activities.examination.fragments.saveall.SaveAllFragment
import com.autocrop.activities.main.MainActivity
import com.autocrop.clearCropBundleList
import com.autocrop.cropBundleList
import com.autocrop.utils.android.SnackbarArgumentRetriever
import com.autocrop.utils.android.displaySnackbar
import com.autocrop.utils.android.intentExtraIdentifier
import com.autocrop.utils.android.returnTransitionAnimation
import com.autocrop.utils.getByBoolean
import com.autocrop.utils.notNull
import com.bunsenbrenner.screenshotboundremoval.R
import com.google.android.material.snackbar.Snackbar


val N_SAVED_CROPS: String = intentExtraIdentifier("n_saved_crops")
private typealias LazyExaminationActivityFragment = Lazy<ExaminationActivityFragment>


class ExaminationActivity : SystemUiHidingFragmentActivity(R.layout.activity_examination) {
    var nSavedCrops: Int = 0
    private lateinit var adWrapper: InterstitialAdWrapper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        adWrapper = InterstitialAdWrapper(
            this,
            "ca-app-pub-3940256099942544/1033173712",  // ca-app-pub-1494255973790385/5182678683
            this::returnToMainActivity,
            true
        )

        val nDismissedImages: Int? =
            retrieveSnackbarArgument(intent, N_DISMISSED_IMAGES_IDENTIFIER, 0)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .add(
                    R.id.container,
                    ExaminationFragment(
                        conductAutoScroll = UserPreferences.conductAutoScroll && cropBundleList.size > 1,
                        longAutoScrollDelay = nDismissedImages.run { notNull() && !equals(0) }
                    )
                )
                .commit()
        }

        with(nDismissedImages) {
            if (notNull())
                displayCropDismissalSnackbar(this!!)
            else if (UserPreferences.conductAutoScroll && cropBundleList.size != 1)
                displaySnackbar(
                    "Tap screen to cancel auto scrolling",
                    R.color.light_gray,
                    Snackbar.LENGTH_SHORT
                )
        }
    }

    val retrieveSnackbarArgument = SnackbarArgumentRetriever()

    private fun displayCropDismissalSnackbar(nDismissedImages: Int) {
        val textColorId: Int = R.color.magenta
        val messageTemplate = "Couldn't find cropping bounds for\n%d image%c"

        when (nDismissedImages) {
            1 -> displaySnackbar(
                messageTemplate.format(1, ""),
                textColorId
            )
            in 2..Int.MAX_VALUE -> displaySnackbar(
                messageTemplate.format(nDismissedImages, "s"),
                textColorId
            )
        }
    }

    private val saveAllFragment: LazyExaminationActivityFragment = lazy { SaveAllFragment() }
    private val appTitleFragment: LazyExaminationActivityFragment = lazy { AppTitleFragment() }

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
            ).getByBoolean(flipRight)
        )
    }

    private val flipRightAnimations: Array<Int> = arrayOf(
        R.animator.card_flip_right_in,
        R.animator.card_flip_right_out
    )

    private fun invokeFragment(
        lazyFragment: LazyExaminationActivityFragment,
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
     * otherwise asks for second one as confirmation;
     *
     * Results in return to main activity
     */
    override fun onBackPressed() {
        when {
            appTitleFragment.isInitialized() -> Unit
            saveAllFragment.isInitialized() -> {
                displaySnackbar("Wait until crops have been saved", R.color.magenta)
            }
            backPressHandler.pressedOnce -> exitActivity()
            else -> {
                backPressHandler.onPress()
                displaySnackbar("Tap again to return to main screen", R.color.light_gray)
            }
        }
    }

    private val backPressHandler = BackPressHandler()

    fun exitActivity() {
        if (nSavedCrops == 0)
            returnToMainActivity()
        else
            adWrapper.showAd()
    }

    /**
     * Clears remaining cropBundle elements contained within cropBundleList
     */
    private fun returnToMainActivity() {
        startActivity(
            Intent(
                this,
                MainActivity::class.java
            ).putExtra(N_SAVED_CROPS, nSavedCrops)
        )

        returnTransitionAnimation()

        clearCropBundleList()
        finishAndRemoveTask()
    }
}