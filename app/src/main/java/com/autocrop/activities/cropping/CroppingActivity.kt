package com.autocrop.activities.cropping

import android.content.Intent
import androidx.lifecycle.ViewModelProvider
import com.autocrop.activities.ActivityTransitions
import com.autocrop.activities.IntentIdentifier
import com.autocrop.activities.cropping.fragments.cropping.CroppingFragment
import com.autocrop.activities.cropping.fragments.croppingfailed.CroppingFailedFragment
import com.autocrop.activities.main.MainActivity
import com.autocrop.uicontroller.activity.FragmentHostingActivity
import com.autocrop.uicontroller.activity.SharedViewModelHandlingActivity
import com.autocrop.utils.android.BackPressHandler
import com.w2sv.autocrop.databinding.ActivityCroppingBinding

class CroppingActivity :
    FragmentHostingActivity<ActivityCroppingBinding>(),
    SharedViewModelHandlingActivity<CroppingActivityViewModel> {

    override lateinit var sharedViewModel: CroppingActivityViewModel

    override val rootFragment: CroppingFragment by lazy{ CroppingFragment() }
    val croppingFailedFragment: CroppingFailedFragment by lazy{ CroppingFailedFragment() }

    override fun onCreateCore() = super.setSharedViewModel()

    override fun provideSharedViewModel(): CroppingActivityViewModel =
        ViewModelProvider(
            this,
            CroppingActivityViewModelFactory(
                uris = intent.getParcelableArrayListExtra(IntentIdentifier.SELECTED_IMAGE_URIS)!!
            )
        )[CroppingActivityViewModel::class.java]

    fun returnToMainActivity() {
        startActivity(
            Intent(this, MainActivity::class.java)
        )
        ActivityTransitions.RETURN(this)
    }

    /**
     * Return to MainActivity on confirmed back press
     */
    private val handleBackPress = BackPressHandler(this, "Tap again to cancel") {
        rootFragment.onStop()
        returnToMainActivity()
    }

    /**
     * Return directly to [MainActivity] if [croppingFailedFragment] visible,
     * otherwise [returnToMainActivity] upon confirmed back press
     */
    override fun onBackPressed() {
        if (croppingFailedFragment.isVisible)
            returnToMainActivity()
        else if (rootFragment.isVisible)
            handleBackPress()
    }
//
//    override fun onStop() {
//        super.onStop()
//
//        finishAndRemoveTask()
//    }
}