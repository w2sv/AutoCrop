package com.w2sv.autocrop.activities.iodetermination

import android.content.Intent
import android.net.Uri
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.ViewModelProvider
import com.w2sv.autocrop.R
import com.w2sv.autocrop.activities.cropping.CropActivity
import com.w2sv.autocrop.activities.iodetermination.fragments.apptitle.AppTitleFragment
import com.w2sv.autocrop.activities.iodetermination.fragments.comparison.ComparisonFragment
import com.w2sv.autocrop.activities.iodetermination.fragments.croppager.CropPagerFragment
import com.w2sv.autocrop.activities.iodetermination.fragments.deletionconfirmation.DeletionConfirmationDialogFragment
import com.w2sv.autocrop.activities.iodetermination.fragments.manualcrop.ManualCropFragment
import com.w2sv.autocrop.activities.iodetermination.fragments.saveall.SaveAllFragment
import com.w2sv.autocrop.controller.activity.ApplicationActivity
import com.w2sv.autocrop.preferences.BooleanPreferences
import com.w2sv.autocrop.utils.android.extensions.getInt
import com.w2sv.autocrop.utils.android.extensions.getParcelableArrayList
import com.w2sv.autocrop.utils.android.extensions.snackyBuilder

class IODeterminationActivity :
    ApplicationActivity<CropPagerFragment, IODeterminationActivityViewModel>(
        CropPagerFragment::class.java,
        IODeterminationActivityViewModel::class.java,
        BooleanPreferences
    ) {

    private companion object {
        const val EXTRA_CROP_URIS = "com.w2sv.autocrop.CROP_URIS"
        const val EXTRA_N_DELETED_SCREENSHOTS = "com.w2sv.autocrop.N_DELETED_SCREENSHOTS"
        const val EXTRA_SAVE_DIR_NAME = "com.w2sv.autocrop.SAVE_DIR_NAME"
    }

    override fun viewModelFactory(): ViewModelProvider.Factory =
        IODeterminationActivityViewModel
            .Factory(nDismissedScreenshots = intent.getInt(CropActivity.EXTRA_N_UNCROPPED_IMAGES))

    /**
     * Invoke [DeletionConfirmationDialogFragment] if there are screenshots whose
     * deletion has to be confirmed, otherwise [AppTitleFragment]
     */
    fun invokeSubsequentFragment() {
        fragmentReplacementTransaction(
            if (viewModel.deletionInquiryUris.isNotEmpty())
                DeletionConfirmationDialogFragment()
            else
                AppTitleFragment(),
            true
        )
            .commit()
    }

    override val onBackPressedCallback: OnBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            getCurrentFragment().let {
                when (it) {
                    is ComparisonFragment -> it.popFromFragmentManager(supportFragmentManager)
                    is ManualCropFragment -> supportFragmentManager.popBackStack()
                    is SaveAllFragment -> {
                        snackyBuilder("Wait until crops have been saved")
                            .setIcon(R.drawable.ic_front_hand_24)
                            .build()
                            .show()
                    }

                    is CropPagerFragment -> it.onBackPress()
                    else -> Unit
                }
            }
        }
    }

    fun startMainActivity() {
        super.startMainActivity(true) {
            it
                .putParcelableArrayListExtra(EXTRA_CROP_URIS, viewModel.writeUris)
                .putExtra(EXTRA_N_DELETED_SCREENSHOTS, viewModel.nDeletedScreenshots)
                .putExtra(EXTRA_SAVE_DIR_NAME, viewModel.cropWriteDirIdentifier(contentResolver))
        }
    }

    data class Results(val cropUris: ArrayList<Uri>, val nDeletedScreenshots: Int, val saveDirName: String?) {
        companion object {
            fun restore(intent: Intent): Results? =
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