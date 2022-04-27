package com.autocrop.activities.examination

import android.content.Intent
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.autocrop.activities.ActivityTransitions
import com.autocrop.activities.IntentExtraIdentifier
import com.autocrop.activities.examination.fragments.apptitle.AppTitleFragment
import com.autocrop.activities.examination.fragments.saveall.SaveAllFragment
import com.autocrop.activities.examination.fragments.sreenshotdeletionquery.ScreenshotDeletionQueryFragment
import com.autocrop.activities.examination.fragments.viewpager.ViewPagerFragment
import com.autocrop.activities.main.MainActivity
import com.autocrop.global.CropFileSaveDestinationPreferences
import com.autocrop.uicontroller.activity.FragmentHostingActivity
import com.autocrop.uicontroller.activity.SharedViewModelHandlingActivity
import com.autocrop.utils.android.BackPressHandler
import com.autocrop.utils.android.displaySnackbar
import com.autocrop.utils.android.uriPermissionGranted
import com.w2sv.autocrop.R
import com.w2sv.autocrop.databinding.ExaminationBinding

class ExaminationActivity :
    FragmentHostingActivity<ExaminationBinding>(),
    SharedViewModelHandlingActivity<ExaminationActivityViewModel> {

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
                validSaveDirDocumentUri = CropFileSaveDestinationPreferences.documentUri?.let{
                    if (uriPermissionGranted(it, Intent.FLAG_GRANT_WRITE_URI_PERMISSION))
                        it
                    else
                        null
                }
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
                    with(sharedViewModel){
                        putExtra(
                            IntentExtraIdentifier.N_SAVED_CROPS_WITH_N_DELETED_SCREENSHOTS,
                            intArrayOf(
                                nSavedCrops,
                                nDeletedScreenshots
                            )
                        )
                        if (nSavedCrops != 0)
                            putExtra(
                                IntentExtraIdentifier.CROP_WRITE_DIR_PATH,
                                cropWriteDirIdentifier()
                            )
                        if (cropSavingUris.isNotEmpty())
                            putParcelableArrayListExtra(
                                IntentExtraIdentifier.CROP_SAVING_URIS,
                                ArrayList(cropSavingUris)
                            )
                    }
                }
        )
        ActivityTransitions.RETURN(this)
    }
}