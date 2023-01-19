package com.w2sv.autocrop.activities.crop

import androidx.fragment.app.Fragment
import com.w2sv.autocrop.activities.AppActivity
import com.w2sv.autocrop.activities.crop.fragments.cropping.CropFragment
import com.w2sv.autocrop.activities.crop.fragments.croppingfailed.CroppingFailedFragment
import com.w2sv.autocrop.activities.main.MainActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CropActivity : AppActivity() {

    override fun getRootFragment(): Fragment =
        CropFragment
            .getInstance(
                @Suppress("DEPRECATION")
                intent.getParcelableArrayListExtra(MainActivity.EXTRA_SELECTED_IMAGE_URIS)!!
            )

    override fun handleOnBackPressed() {
        getCurrentFragment().let {
            when (it) {
                is CroppingFailedFragment -> MainActivity.start(this)
                is CropFragment -> it.onBackPress()
                else -> Unit
            }
        }
    }
}