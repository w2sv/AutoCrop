package com.autocrop.activities.examination

import android.content.Intent
import androidx.lifecycle.ViewModelProvider
import com.autocrop.activities.IntentExtraIdentifier
import com.autocrop.activities.examination.fragments.apptitle.AppTitleFragment
import com.autocrop.activities.examination.fragments.comparison.ComparisonFragment
import com.autocrop.activities.examination.fragments.saveall.SaveAllFragment
import com.autocrop.activities.examination.fragments.sreenshotdeletionquery.ScreenshotDeletionQueryFragment
import com.autocrop.activities.examination.fragments.viewpager.ViewPagerFragment
import com.autocrop.activities.examination.fragments.viewpager.views.MenuInflationButton.Companion.MANUAL_CROP_REQUEST_CODE
import com.autocrop.collections.Crop
import com.autocrop.collections.ImageFileIOSynopsis
import com.autocrop.global.BooleanPreferences
import com.autocrop.global.UriPreferences
import com.autocrop.uicontroller.activity.ApplicationActivity
import com.autocrop.uicontroller.activity.startMainActivity
import com.autocrop.utilsandroid.*
import com.lyrebirdstudio.croppylib.activity.CroppyActivity
import com.w2sv.autocrop.R
import de.mateware.snacky.Snacky

class ExaminationActivity :
    ApplicationActivity<ViewPagerFragment, ExaminationActivityViewModel>(
        ViewPagerFragment::class.java,
        ExaminationActivityViewModel::class,
        accessedPreferenceInstances = arrayOf(BooleanPreferences)) {

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        @Suppress("DEPRECATION")
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK && requestCode == MANUAL_CROP_REQUEST_CODE) {
            data?.let { intent ->
                intent.data?.let { screenshotUri ->
                    castCurrentFragment<ViewPagerFragment>().processAdjustedCrop(
                        Crop.FromScreenshot(
                            screenshot = contentResolver.openBitmap(screenshotUri),
                            rect = CroppyActivity.getCropRect(intent)
                        )
                    )
                    snacky(
                        "Adjusted crop",
                        duration = Snacky.LENGTH_SHORT
                    )
                        .setIcon(R.drawable.ic_baseline_done_24)
                        .buildAndShow()
                }
            }
        }
    }

    override fun viewModelFactory(): ViewModelProvider.Factory =
        ExaminationActivityViewModelFactory(
            validSaveDirDocumentUri = UriPreferences.documentUri?.let{
                if (uriPermissionGranted(it, Intent.FLAG_GRANT_WRITE_URI_PERMISSION))
                    it
                else
                    null
            },
            nDismissedScreenshots = getIntentExtra(IntentExtraIdentifier.N_DISMISSED_IMAGES, blacklistValue = 0)
        )

    //$$$$$$$$$$$$$$$$
    // Post Creation $
    //$$$$$$$$$$$$$$$$

    /**
     * Invoke [ScreenshotDeletionQueryFragment] in case of any screenshot uris whose deletion
     * has to be confirmed, otherwise [AppTitleFragment]
     */
    fun invokeSubsequentFragment() =
        if (sharedViewModel.deletionQueryScreenshotUris.isNotEmpty())
            replaceCurrentFragmentWith(ScreenshotDeletionQueryFragment(), true)
        else
            replaceCurrentFragmentWith(AppTitleFragment(), false)

    /**
     * Block backPress throughout if either [SaveAllFragment] or [AppTitleFragment] showing,
     * otherwise return to MainActivity after confirmation
     */
    private val handleBackPress by lazy {
        BackPressHandler(
            snacky("Tap again to return to main screen"),
            ::returnToMainActivity
        )
    }

    override fun onBackPressed(){
        currentFragment().let {
            when (it) {
                is ComparisonFragment -> {
                    it.prepareExitTransition()
                    supportFragmentManager.popBackStack()
                }
                is SaveAllFragment -> {
                    snacky("Wait until crops have been saved")
                        .setIcon(R.drawable.ic_baseline_front_hand_24)
                        .buildAndShow()
            }
                is ViewPagerFragment -> handleBackPress()
                else -> Unit
            }
        }
    }

    fun returnToMainActivity() {
        startMainActivity{ intent ->
            intent.putExtra(
                IntentExtraIdentifier.EXAMINATION_ACTIVITY_RESULTS,
                ImageFileIOSynopsis(
                    sharedViewModel.nSavedCrops,
                    sharedViewModel.nDeletedScreenshots,
                    sharedViewModel.cropWriteDirIdentifier()
                )
                    .toByteArray()
            )
            if (sharedViewModel.cropSavingUris.isNotEmpty())
                intent.putParcelableArrayListExtra(
                    IntentExtraIdentifier.CROP_SAVING_URIS,
                    ArrayList(sharedViewModel.cropSavingUris)
                )
        }
    }
}