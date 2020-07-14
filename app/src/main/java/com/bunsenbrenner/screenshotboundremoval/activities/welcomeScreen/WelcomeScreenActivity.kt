package com.bunsenbrenner.screenshotboundremoval.activities.welcomeScreen

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import com.bunsenbrenner.screenshotboundremoval.activities.main.MainActivity
import com.bunsenbrenner.screenshotboundremoval.R
import com.bunsenbrenner.screenshotboundremoval.hideSystemUI

class WelcomeScreenActivity: AppCompatActivity(){
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