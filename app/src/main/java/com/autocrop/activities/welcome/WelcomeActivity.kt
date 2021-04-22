package com.autocrop.activities.welcome

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import com.autocrop.activities.SystemUiHidingAppCompatActivity
import com.autocrop.activities.main.MainActivity
import com.bunsenbrenner.screenshotboundremoval.R


class WelcomeActivity : SystemUiHidingAppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)

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