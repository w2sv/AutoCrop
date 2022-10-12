package com.autocrop.activities.cropping

import android.net.Uri
import android.os.Build
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.ViewModelProvider
import com.autocrop.activities.IntentExtraKeys
import com.autocrop.activities.cropping.fragments.cropping.CropFragment
import com.autocrop.activities.cropping.fragments.croppingfailed.CroppingFailedFragment
import com.autocrop.ui.controller.activity.ApplicationActivity
import com.autocrop.utils.android.BackPressHandler
import com.autocrop.utils.android.extensions.snacky

class CropActivity :
    ApplicationActivity<CropFragment, CropActivityViewModel>(
        CropFragment::class,
        CropActivityViewModel::class) {

    override fun viewModelFactory(): ViewModelProvider.Factory =
        CropActivityViewModelFactory(
            uris = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                intent.getParcelableArrayListExtra(IntentExtraKeys.SELECTED_IMAGE_URIS, Uri::class.java)!!
            else
                @Suppress("DEPRECATION")
                intent.getParcelableArrayListExtra(IntentExtraKeys.SELECTED_IMAGE_URIS)!!
        )

    /**
     * Directly [startMainActivity] if [CroppingFailedFragment] visible,
     * otherwise only upon confirmed back press
     */
    override val onBackPressedCallback: OnBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            when (currentFragment()){
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