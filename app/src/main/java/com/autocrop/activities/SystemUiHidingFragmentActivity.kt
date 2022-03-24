package com.autocrop.activities

import androidx.fragment.app.FragmentActivity
import com.autocrop.utils.android.hideSystemUI


abstract class SystemUiHidingFragmentActivity : FragmentActivity() {
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

    private fun hideSystemUI() = hideSystemUI(window)
}
