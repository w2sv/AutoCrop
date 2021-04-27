package com.autocrop.activities.welcome

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import com.autocrop.activities.SystemUiHidingAppCompatActivity
import com.autocrop.activities.main.MainActivity
import com.autocrop.activities.main.PixelField
import com.autocrop.utils.android.debuggingMode
import com.autocrop.utils.android.screenResolution
import com.bunsenbrenner.screenshotboundremoval.R
import timber.log.Timber


class WelcomeActivity : SystemUiHidingAppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (debuggingMode())
            Timber.plant(Timber.DebugTree())

        setContentView(R.layout.activity_welcome)

        PixelField.find_canvas_dimensions(screenResolution(windowManager)).also {
            MainActivity.initializePixelField()
            Timber.i("Initialized pixel field")
        }

        Handler().postDelayed(
            {
                startActivity(
                    Intent(
                        this,
                        MainActivity::class.java
                    )
                ).also {
                    overridePendingTransition(
                        R.anim.fade_in,
                        R.anim.fade_out
                    )
                    finishAndRemoveTask()
                }
            },
            2500
        )
    }
}