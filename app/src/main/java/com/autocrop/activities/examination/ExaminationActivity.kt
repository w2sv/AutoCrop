package com.autocrop.activities.examination

import android.content.Intent
import android.text.SpannableStringBuilder
import androidx.core.text.bold
import androidx.core.text.color
import androidx.lifecycle.ViewModelProvider
import com.autocrop.activities.ActivityTransitions
import com.autocrop.activities.IntentExtraIdentifier
import com.autocrop.activities.examination.fragments.apptitle.AppTitleFragment
import com.autocrop.activities.examination.fragments.comparison.ComparisonFragment
import com.autocrop.activities.examination.fragments.saveall.SaveAllFragment
import com.autocrop.activities.examination.fragments.sreenshotdeletionquery.ScreenshotDeletionQueryFragment
import com.autocrop.activities.examination.fragments.viewpager.ViewPagerFragment
import com.autocrop.activities.examination.fragments.viewpager.views.MenuInflationButton.Companion.MANUAL_CROP_REQUEST_CODE
import com.autocrop.activities.main.MainActivity
import com.autocrop.collections.Crop
import com.autocrop.collections.ImageFileIOSynopsis
import com.autocrop.global.CropSavingPreferences
import com.autocrop.uicontroller.activity.ApplicationActivity
import com.autocrop.utils.numericallyInflected
import com.autocrop.utilsandroid.*
import com.lyrebirdstudio.croppylib.main.CroppyActivity
import com.w2sv.autocrop.R

class ExaminationActivity :
    ApplicationActivity<ViewPagerFragment, ExaminationActivityViewModel>(
        ViewPagerFragment::class.java,
        ExaminationActivityViewModel::class.java) {

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        @Suppress("DEPRECATION")
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK && requestCode == MANUAL_CROP_REQUEST_CODE) {
            data?.let { intent ->
                intent.data?.let { screenshotUri ->
                    castCurrentFragment<ViewPagerFragment>().handleAdjustedCrop(
                        Crop.FromScreenshot(
                            screenshot = contentResolver.openBitmap(screenshotUri),
                            rect = CroppyActivity.getCropRect(intent)
                        )
                    )
                }
            }
        }
    }

    override fun viewModelFactory(): ViewModelProvider.Factory =
        ExaminationActivityViewModelFactory(
            validSaveDirDocumentUri = CropSavingPreferences.documentUri?.let{
                if (uriPermissionGranted(it, Intent.FLAG_GRANT_WRITE_URI_PERMISSION))
                    it
                else
                    null
            }
        )

    override fun triggerEntrySnackbar(){
        intentExtra(IntentExtraIdentifier.N_DISMISSED_IMAGES, blacklistValue = 0)?.let {
            sharedViewModel.autoScrollingDoneListenerConsumable = {
                snacky(
                    SpannableStringBuilder()
                        .append("Couldn't find cropping bounds for ")
                        .bold {
                            color(
                                getColorInt(R.color.accentuated_tv, this@ExaminationActivity)
                            ) { append("$it") }
                        }
                        .append(" image".numericallyInflected(it)),
                    R.drawable.ic_error_24
                )
                    .buildAndShow()
            }
        }
    }

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
    private val handleBackPress = BackPressHandler(this, "Tap again to return to main screen"){
        returnToMainActivity()
    }

    override fun onBackPressed(){
        currentFragment().let {
            when (it) {
                is ComparisonFragment -> {
                    it.prepareExitTransition()
                    supportFragmentManager.popBackStack()
                }
                is SaveAllFragment -> {
                    snacky(
                        "Wait until crops have been saved",
                        R.drawable.ic_baseline_front_hand_24
                    )
                        .buildAndShow()
            }
                is ViewPagerFragment -> handleBackPress()
                else -> Unit
            }
        }
    }

    fun returnToMainActivity(){
        startActivity(
            Intent(
                this,
                MainActivity::class.java
            )
                .apply {
                    putExtra(
                        IntentExtraIdentifier.EXAMINATION_ACTIVITY_RESULTS,
                        ImageFileIOSynopsis(
                            sharedViewModel.nSavedCrops,
                            sharedViewModel.nDeletedScreenshots,
                            sharedViewModel.cropWriteDirIdentifier()
                        )
                            .toByteArray()
                    )
                    if (sharedViewModel.cropSavingUris.isNotEmpty())
                        putParcelableArrayListExtra(
                            IntentExtraIdentifier.CROP_SAVING_URIS,
                            ArrayList(sharedViewModel.cropSavingUris)
                        )
                }
        )
        ActivityTransitions.RETURN(this)
    }
}