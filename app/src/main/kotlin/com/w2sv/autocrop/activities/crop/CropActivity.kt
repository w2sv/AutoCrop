package com.w2sv.autocrop.activities.crop

import androidx.fragment.app.Fragment
import com.w2sv.androidutils.os.getParcelableArrayListCompat
import com.w2sv.autocrop.activities.AppActivity
import com.w2sv.autocrop.activities.main.MainActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint  // Resolves java.lang.IllegalStateException: Hilt Fragments must be attached to an @AndroidEntryPoint Activity
class CropActivity : AppActivity() {

    override fun getRootFragment(): Fragment =
        CropFragment
            .getInstance(
                intent.getParcelableArrayListCompat(MainActivity.EXTRA_SELECTED_IMAGE_URIS)!!
            )

    override fun handleOnBackPressed() {
        when (val fragment = getCurrentFragment()) {
            is CroppingFailedFragment -> MainActivity.start(this)
            is CropFragment -> fragment.onBackPress()
            else -> throw IllegalStateException("Invalid Fragment type")
        }
    }
}