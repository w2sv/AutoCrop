package com.autocrop.activities.iodetermination

import android.content.Intent
import android.net.Uri
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.ViewModelProvider
import com.autocrop.activities.iodetermination.fragments.apptitle.AppTitleFragment
import com.autocrop.activities.iodetermination.fragments.comparison.ComparisonFragment
import com.autocrop.activities.iodetermination.fragments.croppager.CropPagerFragment
import com.autocrop.activities.iodetermination.fragments.deletionconfirmation.DeletionConfirmationDialogFragment
import com.autocrop.activities.iodetermination.fragments.manualcrop.ManualCropFragment
import com.autocrop.activities.iodetermination.fragments.saveall.SaveAllFragment
import com.autocrop.activities.main.MainActivity
import com.autocrop.preferences.BooleanPreferences
import com.autocrop.preferences.UriPreferences
import com.autocrop.uicontroller.activity.ApplicationActivity
import com.autocrop.utils.android.BackPressHandler
import com.autocrop.utils.android.extensions.getInt
import com.autocrop.utils.android.extensions.getParcelableArrayList
import com.autocrop.utils.android.extensions.show
import com.autocrop.utils.android.extensions.snacky
import com.w2sv.autocrop.R

class IODeterminationActivity :
    ApplicationActivity<CropPagerFragment, IODeterminationActivityViewModel>(
        CropPagerFragment::class,
        IODeterminationActivityViewModel::class,
        BooleanPreferences) {

    private companion object{
        const val EXTRA_CROP_URIS = "com.w2sv.autocrop.CROP_URIS"
        const val EXTRA_N_DELETED_SCREENSHOTS = "com.w2sv.autocrop.N_DELETED_SCREENSHOTS"
        const val EXTRA_SAVE_DIR_NAME = "com.w2sv.autocrop.SAVE_DIR_NAME"
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
            if (viewModel.deletionInquiryUris.isNotEmpty())
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
                        it.binding.comparisonIv.prepareSharedElementExitTransition()
                        supportFragmentManager.popBackStack()
                    }
                    is ManualCropFragment -> supportFragmentManager.popBackStack()
                    is SaveAllFragment -> {
                        snacky("Wait until crops have been saved")
                            .setIcon(R.drawable.ic_front_hand_24)
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
            with(intent){
                putParcelableArrayListExtra(EXTRA_CROP_URIS, viewModel.writeUris)
                putExtra(EXTRA_N_DELETED_SCREENSHOTS, viewModel.nDeletedScreenshots)
                putExtra(EXTRA_SAVE_DIR_NAME, viewModel.cropWriteDirIdentifier(contentResolver))
            }
        }
    }

    data class Results(val cropUris: ArrayList<Uri>, val nDeletedScreenshots: Int, val saveDirName: String?){
        companion object{
            fun attemptRestoration(intent: Intent): Results? =
                intent.run {
                    if (hasExtra(EXTRA_CROP_URIS))
                        Results(
                            getParcelableArrayList(EXTRA_CROP_URIS)!!,
                            getInt(EXTRA_N_DELETED_SCREENSHOTS),
                            getStringExtra(EXTRA_SAVE_DIR_NAME)
                        )
                    else
                        null
                }
        }

        val nSavedCrops: Int get() = cropUris.size
    }
}