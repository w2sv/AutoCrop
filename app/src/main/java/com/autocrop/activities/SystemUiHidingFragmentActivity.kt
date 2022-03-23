package com.autocrop.activities

import androidx.fragment.app.FragmentActivity
import com.autocrop.utils.android.hideSystemUI


abstract class SystemUiHidingFragmentActivity(layoutId: Int) : FragmentActivity(layoutId) {
    private fun hideSystemUI() {
        hideSystemUI(window)
    }

    override fun onStart() {
        super.onStart()
        hideSystemUI()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        hideSystemUI()
    }

    override fun onResume() {
        super.onResume()
        hideSystemUI()
    }
}