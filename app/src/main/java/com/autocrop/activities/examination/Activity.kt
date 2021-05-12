/**
 * ViewPager2 Reference: https://medium.com/swlh/android-infinite-auto-image-slider-using-view-pager-2-android-studio-java-a0e450dec071
 */

package com.autocrop.activities.examination

import android.content.Intent
import android.os.Bundle
import android.view.animation.AnimationSet
import com.autocrop.activities.BackPressHandler
import com.autocrop.activities.SystemUiHidingFragmentActivity
import com.autocrop.activities.cropping.N_DISMISSED_IMAGES_IDENTIFIER
import com.autocrop.activities.examination.fragments.ExaminationActivityFragment
import com.autocrop.activities.examination.fragments.apptitle.AppTitleFragment
import com.autocrop.activities.examination.fragments.saveall.SaveAllFragment
import com.autocrop.activities.examination.fragments.examination.ExaminationFragment
import com.autocrop.activities.main.MainActivity
import com.autocrop.clearCropBundleList
import com.autocrop.utils.android.*
import com.autocrop.utils.getByBoolean
import com.autocrop.utils.toInt
import com.bunsenbrenner.screenshotboundremoval.R


val N_SAVED_CROPS: String = intentExtraIdentifier("n_saved_crops")
private typealias LazyExaminationActivityFragment = Lazy<ExaminationActivityFragment>


class ExaminationActivity : SystemUiHidingFragmentActivity(R.layout.activity_examination) {
    var nSavedCrops: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .add(R.id.container, ExaminationFragment())
                .commit()
        }

        retrieveSnackbarArgument(intent, N_DISMISSED_IMAGES_IDENTIFIER, 0)?.let {
            displayCropDismissalToast(it)
        }
    }

    val retrieveSnackbarArgument = SnackbarArgumentRetriever()

    private fun displayCropDismissalToast(nDismissedImages: Int) {
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

    private val saveAllFragment: LazyExaminationActivityFragment = lazy { SaveAllFragment() }
    private val appTitleFragment: LazyExaminationActivityFragment = lazy { AppTitleFragment() }

    fun invokeSaveAllFragment() {
        invokeFragment(
            saveAllFragment,
            flipRightAnimations
        )
    }

    fun invokeAppTitleFragment(flipRight: Boolean){
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

    private fun invokeFragment(lazyFragment: LazyExaminationActivityFragment, animations: Array<Int>){
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
        when{
            appTitleFragment.isInitialized() -> Unit
            saveAllFragment.isInitialized() -> {
                displayToast("Please wait until crops\nhave been saved")
            }
            backPressHandler.pressedOnce -> returnToMainActivity()
            else -> {
                backPressHandler.onPress()
                displayToast("Tap again to return to main screen")
            }
        }
    }

    private val backPressHandler = BackPressHandler()

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

        clearCropBundleList()
        finishAndRemoveTask()
    }
}