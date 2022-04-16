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
import com.autocrop.activities.examination.fragments.sreenshotdeletionquery.ScreenshotDeletionQueryFragment
import com.autocrop.activities.examination.fragments.viewpager.ViewPagerFragment
import com.autocrop.activities.main.MainActivity
import com.autocrop.global.CropFileSaveDestinationPreferences
import com.autocrop.uicontroller.activity.FragmentHostingActivity
import com.autocrop.utils.android.*
import com.autocrop.utils.numberInflection
import com.w2sv.autocrop.R
import com.w2sv.autocrop.databinding.ActivityExaminationBinding
import timber.log.Timber

class ExaminationActivity : FragmentHostingActivity<ActivityExaminationBinding>() {

    private lateinit var sharedViewModel: ExaminationActivityViewModel

    private val nDismissedImagesRetriever = IntentExtraRetriever<Int>(IntentIdentifier.N_DISMISSED_IMAGES)

    override val rootFragment: ViewPagerFragment by lazy{ViewPagerFragment()}
    val saveAllFragment: SaveAllFragment by lazy { SaveAllFragment() }
    val appTitleFragment: AppTitleFragment by lazy { AppTitleFragment() }
    private val screenshotDeletionQueryFragment: ScreenshotDeletionQueryFragment by lazy { ScreenshotDeletionQueryFragment() }

    override fun onCreateCore() {

        // retrieve ViewModel
        sharedViewModel = ViewModelProvider(
            this,
            ExaminationViewModelFactory(
                nDismissedImagesRetriever(intent) ?: 0,
                CropFileSaveDestinationPreferences.documentUri?.let{uriPermissionGranted(it, Intent.FLAG_GRANT_WRITE_URI_PERMISSION)}
            )
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

    fun redirectToExitFragment(){
        if (sharedViewModel.deletionQueryScreenshotUris.isNotEmpty())
            replaceCurrentFragmentWith(screenshotDeletionQueryFragment, true)
                .also { Timber.i("Invoking screenshotDeletionQueryFragment for ${sharedViewModel.deletionQueryScreenshotUris.size} deletion uris") }
        else
            replaceCurrentFragmentWith(appTitleFragment, false)
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
                NotificationColor.NEUTRAL
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
                .apply {
                    putExtra(
                        IntentIdentifier.N_SAVED_CROPS_WITH_N_DELETED_SCREENSHOTS,
                        intArrayOf(
                            sharedViewModel.nSavedCrops,
                            sharedViewModel.nDeletedScreenshots
                        )
                    )
                    if (sharedViewModel.nSavedCrops != 0)
                        putExtra(
                            IntentIdentifier.CROP_WRITE_DIR_PATH,
                            sharedViewModel.cropWriteDirIdentifier()
                        )
                }
        )
        ActivityTransitions.RETURN(this)
    }
}