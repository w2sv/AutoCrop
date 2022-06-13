package com.autocrop.activities.cropping

import androidx.lifecycle.ViewModelProvider
import com.autocrop.activities.IntentExtraIdentifier
import com.autocrop.activities.cropping.fragments.cropping.CroppingFragment
import com.autocrop.activities.cropping.fragments.croppingfailed.CroppingFailedFragment
import com.autocrop.uicontroller.activity.ApplicationActivity
import com.autocrop.uicontroller.activity.startMainActivity
import com.autocrop.utilsandroid.BackPressHandler
import com.autocrop.utilsandroid.snacky

class CroppingActivity :
    ApplicationActivity<CroppingFragment, CroppingActivityViewModel>(
        CroppingFragment::class.java,
        CroppingActivityViewModel::class.java) {

    override fun viewModelFactory(): ViewModelProvider.Factory =
        CroppingActivityViewModelFactory(
            uris = intent.getParcelableArrayListExtra(IntentExtraIdentifier.SELECTED_IMAGE_URIS)!!
        )

    /**
     * Directly [startMainActivity] if [CroppingFailedFragment] visible,
     * otherwise only upon confirmed back press
     */
    override fun onBackPressed() {
        when (currentFragment()){
            is CroppingFailedFragment -> startMainActivity()
            is CroppingFragment -> handleBackPress()
            else -> Unit
        }
    }

    /**
     * Return to MainActivity on confirmed back press
     */
    private val handleBackPress = BackPressHandler(
        snacky("Tap again to cancel"),
        ::startMainActivity
    )
}