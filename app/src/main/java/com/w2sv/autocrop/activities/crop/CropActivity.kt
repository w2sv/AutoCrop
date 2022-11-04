package com.w2sv.autocrop.activities.crop

import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.ViewModelProvider
import com.w2sv.autocrop.activities.crop.fragments.cropping.CropFragment
import com.w2sv.autocrop.activities.crop.fragments.croppingfailed.CroppingFailedFragment
import com.w2sv.autocrop.activities.main.MainActivity
import com.w2sv.autocrop.controller.activity.ApplicationActivity
import com.w2sv.autocrop.utils.android.extensions.getParcelableArrayList

class CropActivity :
    ApplicationActivity<CropFragment, CropActivityViewModel>(
        CropFragment::class.java,
        CropActivityViewModel::class.java
    ) {

    companion object {
        const val EXTRA_N_UNCROPPED_IMAGES = "com.w2sv.autocrop.extra.N_UNCROPPED_IMAGES"
    }

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
            getCurrentFragment().let {
                when (it) {
                    is CroppingFailedFragment -> startMainActivity()
                    is CropFragment -> it.onBackPress()
                    else -> Unit
                }
            }
        }
    }
}