/**
 * ViewPager2 Reference: https://medium.com/swlh/android-infinite-auto-image-slider-using-view-pager-2-android-studio-java-a0e450dec071
 */

package com.autocrop.activities.examination

import android.content.Intent
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.autocrop.activities.ActivityTransitions
import com.autocrop.activities.IntentIdentifier
import com.autocrop.activities.examination.fragments.apptitle.AppTitleFragment
import com.autocrop.activities.examination.fragments.saveall.SaveAllFragment
import com.autocrop.activities.examination.fragments.viewpager.ViewPagerFragment
import com.autocrop.activities.main.MainActivity
import com.autocrop.uicontroller.activity.FragmentHostingActivity
import com.autocrop.utils.android.*
import com.autocrop.utils.logAfterwards
import com.w2sv.autocrop.R
import com.w2sv.autocrop.databinding.ActivityExaminationBinding

class ExaminationActivity : FragmentHostingActivity<ActivityExaminationBinding>() {

    private lateinit var sharedViewModel: ExaminationActivityViewModel

    private val nDismissedImagesRetriever = IntentExtraRetriever<Int>(IntentIdentifier.N_DISMISSED_IMAGES)

    override val rootFragment: ViewPagerFragment by lazy{ViewPagerFragment()}
    val saveAllFragment: SaveAllFragment by lazy { SaveAllFragment() }
    val appTitleFragment: AppTitleFragment by lazy { AppTitleFragment() }

    override fun onCreateCore() {
        // retrieve ViewModel
        sharedViewModel = ViewModelProvider(
            this,
            ExaminationViewModelFactory(nDismissedImages = nDismissedImagesRetriever(intent) ?: 0)
        )[ExaminationActivityViewModel::class.java]

        // display Snackbar
        with(sharedViewModel.nDismissedImages) {
            if (!equals(0))
                displaySnackbar(
                    "Couldn't find cropping bounds for\n$this image${numberInflection(this)}",
                    NotificationColor.URGENT,
                    2500
                )
        }
    }

    fun replaceCurrentFragmentWith(fragment: Fragment, flipRight: Boolean) {
        super.replaceCurrentFragmentWith(
            fragment,
            if (flipRight)
                R.animator.card_flip_left_in to R.animator.card_flip_left_out
            else
                R.animator.card_flip_right_in to R.animator.card_flip_right_out
        )
    }

    /**
     * Block backPress throughout if either [saveAllFragment] or [appTitleFragment] showing,
     * otherwise return to MainActivity after confirmation
     */
    private val handleBackPress = BackPressHandler(this, "Tap again to return to main screen"){
        returnToMainActivity()
    }

    override fun onBackPressed() = when {
        appTitleFragment.isVisible -> Unit
        saveAllFragment.isVisible -> {
            displaySnackbar(
                "Wait until crops have been saved",
                NotificationColor.URGENT
            )
        }
        else -> handleBackPress()
    }

    fun returnToMainActivity(){
        startActivity(
            Intent(
                this,
                MainActivity::class.java
            )
                .putExtra(
                    IntentIdentifier.N_SAVED_CROPS_WITH_N_DELETED_SCREENSHOTS,
                    intArrayOf(
                        sharedViewModel.nSavedCrops,
                        sharedViewModel.nDeletedScreenshots
                    )
                )
                .putExtra(
                    IntentIdentifier.CROP_WRITE_DIR_PATH,
                    sharedViewModel.cropWriteDirPath
                )
        )
        ActivityTransitions.RETURN(this)
    }

    override fun onStop() = logAfterwards("Cleared cropBundles") {
        super.onStop()

        ExaminationActivityViewModel.cropBundles.clear()
        finishAndRemoveTask()
    }
}