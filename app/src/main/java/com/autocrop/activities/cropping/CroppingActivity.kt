package com.autocrop.activities.cropping

import android.content.Intent
import androidx.lifecycle.ViewModelProvider
import com.autocrop.activities.ActivityTransitions
import com.autocrop.activities.IntentExtraIdentifier
import com.autocrop.activities.cropping.fragments.cropping.CroppingFragment
import com.autocrop.activities.cropping.fragments.croppingfailed.CroppingFailedFragment
import com.autocrop.activities.main.MainActivity
import com.autocrop.uicontroller.activity.FragmentHostingActivity
import com.autocrop.utils.android.BackPressHandler
import com.w2sv.autocrop.databinding.CroppingBinding

class CroppingActivity :
    FragmentHostingActivity<CroppingBinding, CroppingFragment, CroppingActivityViewModel>(
        CroppingFragment::class.java,
        CroppingActivityViewModel::class.java) {

    override fun viewModelFactory(): ViewModelProvider.Factory =
        CroppingActivityViewModelFactory(
            uris = intent.getParcelableArrayListExtra(IntentExtraIdentifier.SELECTED_IMAGE_URIS)!!
        )

    fun returnToMainActivity() {
        startActivity(
            Intent(this, MainActivity::class.java)
        )
        ActivityTransitions.RETURN(this)
    }

    /**
     * Return to MainActivity on confirmed back press
     */
    private val handleBackPress = BackPressHandler(
        this,
        "Tap again to cancel",
        ::returnToMainActivity
    )

    /**
     * Directly [returnToMainActivity] if [CroppingFailedFragment] visible,
     * otherwise [returnToMainActivity] upon confirmed back press
     */
    override fun onBackPressed() {
        when (currentFragment()){
            is CroppingFailedFragment -> returnToMainActivity()
            is CroppingFragment -> handleBackPress()
            else -> Unit
        }
    }
}