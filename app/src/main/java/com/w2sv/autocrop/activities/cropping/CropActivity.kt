package com.w2sv.autocrop.activities.cropping

import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.ViewModelProvider
import com.w2sv.autocrop.activities.cropping.fragments.cropping.CropFragment
import com.w2sv.autocrop.activities.cropping.fragments.croppingfailed.CroppingFailedFragment
import com.w2sv.autocrop.activities.main.MainActivity
import com.w2sv.autocrop.controller.activity.ApplicationActivity
import com.w2sv.autocrop.utils.android.BackPressHandler
import com.w2sv.autocrop.utils.android.extensions.getParcelableArrayList
import com.w2sv.autocrop.utils.android.extensions.snackyBuilder

class CropActivity :
    ApplicationActivity<CropFragment, CropActivityViewModel>(
        CropFragment::class,
        CropActivityViewModel::class
    ) {

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
            when (getCurrentFragment()) {
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
            snackyBuilder("Tap again to cancel"),
            ::startMainActivity
        )
    }
}