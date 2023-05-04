package com.w2sv.autocrop.activities.main

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.w2sv.autocrop.activities.AppActivity
import com.w2sv.autocrop.activities.main.fragments.about.AboutFragment
import com.w2sv.autocrop.activities.main.fragments.flowfield.FlowFieldFragment
import com.w2sv.autocrop.domain.AccumulatedIOResults
import com.w2sv.common.extensions.getParcelableExtraCompat
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppActivity() {

    companion object {
        const val EXTRA_SELECTED_IMAGE_URIS = "com.w2sv.autocrop.extra.SELECTED_IMAGE_URIS"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportFragmentManager.addOnBackStackChangedListener {
            when (val fragment = getCurrentFragment()) {
                is FlowFieldFragment -> fragment.binding.navigationViewToggleButton.progress = 0f
            }
        }
    }

    //////////////////////////////////////
    //   ApplicationActivity overrides  //
    //////////////////////////////////////

    override fun getRootFragment(): Fragment =
        FlowFieldFragment.getInstance(intent.getParcelableExtraCompat(AccumulatedIOResults.EXTRA))

    override fun handleOnBackPressed() {
        when (val fragment = getCurrentFragment()) {
            is AboutFragment -> supportFragmentManager.popBackStack()
            is FlowFieldFragment -> fragment.onBackPress()
            else -> Unit
        }
    }
}