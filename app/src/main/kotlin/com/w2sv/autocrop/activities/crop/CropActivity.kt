package com.w2sv.autocrop.activities.crop

import androidx.fragment.app.Fragment
import com.w2sv.androidutils.generic.getParcelableArrayListCompat
import com.w2sv.autocrop.activities.AppActivity
import com.w2sv.autocrop.activities.main.MainActivity
import com.w2sv.autocrop.utils.extensions.startMainActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint  // java.lang.IllegalStateException: Hilt Fragments must be attached to an @AndroidEntryPoint Activity
class CropActivity : AppActivity() {

    override fun getRootFragment(): Fragment =
        CropFragment
            .getInstance(
                intent.getParcelableArrayListCompat(MainActivity.EXTRA_SELECTED_IMAGE_URIS)!!
            )

    override fun handleOnBackPressed() {
        when (val fragment = getCurrentFragment()) {
            is CroppingFailedFragment -> startMainActivity()
            is CropFragment -> fragment.onBackPress()
            else -> Unit
        }
    }
}