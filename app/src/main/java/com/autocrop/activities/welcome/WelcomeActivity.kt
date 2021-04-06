package com.autocrop.activities.welcome

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity

import com.autocrop.activities.main.MainActivity
import com.autocrop.hideSystemUI
import com.bunsenbrenner.screenshotboundremoval.R


class WelcomeActivity: AppCompatActivity(){
    override fun onStart() {
        super.onStart()
        hideSystemUI(window)
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        hideSystemUI(window)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.welcome_screen)

        Handler().postDelayed({
            MainActivity.initializePixelField(windowManager)
            startActivity(Intent(this, MainActivity::class.java))
            overridePendingTransition(
                R.anim.fade_in,
                R.anim.fade_out
            )
            finish()
        }, 2000)
    }
}