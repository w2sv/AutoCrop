package com.w2sv.autocrop.activities.crop

import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import com.w2sv.autocrop.activities.ApplicationActivity
import com.w2sv.autocrop.activities.crop.fragments.cropping.CropFragment
import com.w2sv.autocrop.activities.crop.fragments.croppingfailed.CroppingFailedFragment
import com.w2sv.autocrop.activities.main.MainActivity

class CropActivity : ApplicationActivity() {

    companion object {
        const val EXTRA_N_UNCROPPED_IMAGES = "com.w2sv.autocrop.extra.N_UNCROPPED_IMAGES"
    }

    override fun getRootFragment(): Fragment =
        @Suppress("DEPRECATION")
        CropFragment
            .getInstance(
                intent.getParcelableArrayListExtra(MainActivity.EXTRA_SELECTED_IMAGE_URIS)!!
            )

    override val onBackPressedCallback: OnBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            getCurrentFragment().let {
                when (it) {
                    is CroppingFailedFragment -> MainActivity.restart(this@CropActivity)
                    is CropFragment -> it.onBackPress()
                    else -> Unit
                }
            }
        }
    }
}