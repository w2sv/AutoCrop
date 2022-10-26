package com.autocrop.activities.cropping

import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.ViewModelProvider
import com.autocrop.activities.cropping.fragments.cropping.CropFragment
import com.autocrop.activities.cropping.fragments.croppingfailed.CroppingFailedFragment
import com.autocrop.activities.main.MainActivity
import com.autocrop.uicontroller.activity.ApplicationActivity
import com.autocrop.utils.android.BackPressHandler
import com.autocrop.utils.android.extensions.getParcelableArrayList
import com.autocrop.utils.android.extensions.snacky

class CropActivity :
    ApplicationActivity<CropFragment, CropActivityViewModel>(
        CropFragment::class,
        CropActivityViewModel::class) {

    override fun viewModelFactory(): ViewModelProvider.Factory =
        CropActivityViewModel.Factory(
            uris = intent.getParcelableArrayList(MainActivity.EXTRA_SELECTED_IMAGE_URIS)!!
        )

    /**
     * Directly [startMainActivity] if [CroppingFailedFragment] visible,
     * otherwise only upon confirmed back press
     */
    override val onBackPressedCallback: OnBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            when (getCurrentFragment()){
                is CroppingFailedFragment -> startMainActivity()
                is CropFragment -> handleBackPress()
                else -> Unit
            }
        }
    }

    /**
     * Return to MainActivity on confirmed back press
     */
    private val handleBackPress by lazy {
        BackPressHandler(
            snacky("Tap again to cancel"),
            ::startMainActivity
        )
    }
}