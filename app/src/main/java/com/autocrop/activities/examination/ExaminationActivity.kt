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
import com.autocrop.uicontroller.activity.SharedViewModelHandler
import com.autocrop.utils.android.BackPressHandler
import com.autocrop.utils.android.displaySnackbar
import com.autocrop.utils.android.uriPermissionGranted
import com.w2sv.autocrop.R
import com.w2sv.autocrop.databinding.ActivityExaminationBinding

class ExaminationActivity :
    FragmentHostingActivity<ActivityExaminationBinding>(),
    SharedViewModelHandler<ExaminationActivityViewModel> {

    override val rootFragment: ViewPagerFragment by lazy{ ViewPagerFragment() }
    val saveAllFragment: SaveAllFragment by lazy { SaveAllFragment() }
    private val screenshotDeletionQueryFragment: ScreenshotDeletionQueryFragment by lazy { ScreenshotDeletionQueryFragment() }
    val appTitleFragment: AppTitleFragment by lazy { AppTitleFragment() }

    override lateinit var sharedViewModel: ExaminationActivityViewModel

    override fun onCreateCore() = super.setSharedViewModel()

    override fun provideSharedViewModel(): ExaminationActivityViewModel =
        ViewModelProvider(
            this,
            ExaminationViewModelFactory(
                documentUriWritePermissionValid = CropFileSaveDestinationPreferences.documentUri?.let{uriPermissionGranted(it, Intent.FLAG_GRANT_WRITE_URI_PERMISSION)}
            )
        )[ExaminationActivityViewModel::class.java]

    //$$$$$$$$$$$$$$$$
    // Post Creation $
    //$$$$$$$$$$$$$$$$

    fun replaceCurrentFragmentWith(fragment: Fragment, flipRight: Boolean) =
        super.replaceCurrentFragmentWith(
            fragment,
            if (flipRight)
                R.animator.card_flip_left_in to R.animator.card_flip_left_out
            else
                R.animator.card_flip_right_in to R.animator.card_flip_right_out
        )

    /**
     * Invoke [screenshotDeletionQueryFragment] in case of any screenshot uris whose deletion
     * has to be confirmed, otherwise [appTitleFragment]
     */
    fun invokeSubsequentFragment() =
        if (sharedViewModel.deletionQueryScreenshotUris.isNotEmpty())
            replaceCurrentFragmentWith(screenshotDeletionQueryFragment, true)
        else
            replaceCurrentFragmentWith(appTitleFragment, false)

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
                R.drawable.ic_baseline_front_hand_24
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