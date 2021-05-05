package com.autocrop.activities.welcome

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import com.autocrop.activities.SystemUiHidingAppCompatActivity
import com.autocrop.activities.main.MainActivity
import com.autocrop.utils.android.debuggingModeEnabled
import com.bunsenbrenner.screenshotboundremoval.R
import timber.log.Timber


class Activity : SystemUiHidingAppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (debuggingModeEnabled())
            Timber.plant(Timber.DebugTree())

        setContentView(R.layout.activity_welcome)

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
                    finish()
                }
            },
            500  // TODO
        )
    }
}