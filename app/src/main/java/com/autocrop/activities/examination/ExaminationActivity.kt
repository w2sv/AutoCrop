package com.autocrop.activities.examination

import android.content.Intent
import android.text.SpannableStringBuilder
import androidx.core.text.bold
import androidx.core.text.color
import androidx.lifecycle.ViewModelProvider
import com.autocrop.activities.ActivityTransitions
import com.autocrop.activities.IntentExtraIdentifier
import com.autocrop.activities.examination.fragments.apptitle.AppTitleFragment
import com.autocrop.activities.examination.fragments.saveall.SaveAllFragment
import com.autocrop.activities.examination.fragments.sreenshotdeletionquery.ScreenshotDeletionQueryFragment
import com.autocrop.activities.examination.fragments.viewpager.ViewPagerFragment
import com.autocrop.activities.main.MainActivity
import com.autocrop.global.CropFileSaveDestinationPreferences
import com.autocrop.uicontroller.activity.ApplicationActivity
import com.autocrop.utils.android.*
import com.autocrop.utils.numericallyInflected
import com.w2sv.autocrop.R
import com.w2sv.autocrop.databinding.ExaminationBinding

class ExaminationActivity :
    ApplicationActivity<ExaminationBinding, ViewPagerFragment, ExaminationActivityViewModel>(
        ViewPagerFragment::class.java,
        ExaminationActivityViewModel::class.java) {

    override fun viewModelFactory(): ViewModelProvider.Factory =
        ExaminationActivityViewModelFactory(
            validSaveDirDocumentUri = CropFileSaveDestinationPreferences.documentUri?.let{
                if (uriPermissionGranted(it, Intent.FLAG_GRANT_WRITE_URI_PERMISSION))
                    it
                else
                    null
            }
        )

    override fun displayEntrySnackbar(){
        intentExtra(IntentExtraIdentifier.N_DISMISSED_IMAGES, blacklistValue = 0)?.let {
            snacky(
                SpannableStringBuilder()
                    .append("Couldn't find cropping bounds for ")
                    .bold {
                        color(
                            getColorInt(R.color.holo_purple, this@ExaminationActivity)
                        ) { append("$it") }
                    }
                    .append(" image".numericallyInflected(it)),
                R.drawable.ic_error_24
            )
                .show()
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
        supportFragmentManager.findFragmentById(binding.root.id)?.let { currentFragment ->
            when (currentFragment) {
                is AppTitleFragment -> Unit
                is SaveAllFragment -> {
                    snacky(
                        "Wait until crops have been saved",
                        R.drawable.ic_baseline_front_hand_24
                    )
                        .show()
                }
                else -> handleBackPress()
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
                    with(sharedViewModel){
                        putExtra(
                            IntentExtraIdentifier.N_SAVED_CROPS_WITH_N_DELETED_SCREENSHOTS,
                            intArrayOf(
                                nSavedCrops,
                                nDeletedScreenshots
                            )
                        )
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