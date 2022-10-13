package com.autocrop.activities.iodetermination

import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.ViewModelProvider
import com.autocrop.activities.iodetermination.fragments.apptitle.AppTitleFragment
import com.autocrop.activities.iodetermination.fragments.comparison.ComparisonFragment
import com.autocrop.activities.iodetermination.fragments.croppager.CropPagerFragment
import com.autocrop.activities.iodetermination.fragments.deletionconfirmationdialog.DeletionConfirmationDialogFragment
import com.autocrop.activities.iodetermination.fragments.saveall.SaveAllFragment
import com.autocrop.activities.main.MainActivity
import com.autocrop.dataclasses.IOSynopsis
import com.autocrop.preferences.BooleanPreferences
import com.autocrop.preferences.UriPreferences
import com.autocrop.ui.controller.activity.ApplicationActivity
import com.autocrop.utils.android.BackPressHandler
import com.autocrop.utils.android.extensions.getInt
import com.autocrop.utils.android.extensions.show
import com.autocrop.utils.android.extensions.snacky
import com.w2sv.autocrop.R

class IODeterminationActivity :
    ApplicationActivity<CropPagerFragment, IODeterminationActivityViewModel>(
        CropPagerFragment::class,
        IODeterminationActivityViewModel::class,
        BooleanPreferences) {

    companion object{
        const val EXTRA_IO_SYNOPSIS = "com.autocrop.IO_SYNOPSIS"
        const val EXTRA_CROP_SAVING_URIS = "com.autocrop.CROP_SAVING_URIS"
    }

    override fun viewModelFactory(): ViewModelProvider.Factory =
        IODeterminationActivityViewModelFactory(
            validSaveDirDocumentUri = UriPreferences.validDocumentUri(this),
            nDismissedScreenshots = intent.getInt(MainActivity.EXTRA_N_DISMISSED_IMAGES)
        )

    //$$$$$$$$$$$$$$$$
    // Post Creation $
    //$$$$$$$$$$$$$$$$

    /**
     * Invoke [DeletionConfirmationDialogFragment] if there are screenshots whose
     * deletion has to be confirmed, otherwise [AppTitleFragment]
     */
    fun invokeSubsequentFragment(){
        fragmentReplacementTransaction(
            if (viewModel.screenshotDeletionInquiryUris.isNotEmpty())
                DeletionConfirmationDialogFragment()
            else
                AppTitleFragment(),
            false
        )
            .commit()
    }

    /**
     * Block backPress throughout if either [SaveAllFragment] or [AppTitleFragment] showing,
     * otherwise return to MainActivity after confirmation
     */
    private val handleBackPress by lazy {
        BackPressHandler(
            snacky("Tap again to return to main screen"),
            ::startMainActivity
        )
    }

    override val onBackPressedCallback: OnBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            getCurrentFragment().let {
                when (it) {
                    is ComparisonFragment -> {
                        it.prepareExitTransition()
                        supportFragmentManager.popBackStack()
                    }
                    is SaveAllFragment -> {
                        snacky("Wait until crops have been saved")
                            .setIcon(R.drawable.ic_baseline_front_hand_24)
                            .show()
                    }
                    is CropPagerFragment -> handleBackPress()
                    else -> Unit
                }
            }
        }
    }

    fun startMainActivity() {
        startMainActivity{ intent ->
            if (viewModel.savedCropUris.isNotEmpty())
                intent.putParcelableArrayListExtra(
                    EXTRA_CROP_SAVING_URIS,
                    ArrayList(viewModel.savedCropUris)
                )
            intent.putExtra(
                EXTRA_IO_SYNOPSIS,
                IOSynopsis(
                    viewModel.savedCropUris.size,
                    viewModel.nDeletedScreenshots,
                    viewModel.cropWriteDirIdentifier()
                )
                    .toByteArray()
            )
        }
    }
}