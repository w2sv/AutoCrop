package com.autocrop.activities.welcome

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import com.autocrop.activities.SystemUiHidingFragmentActivity
import com.autocrop.activities.main.MainActivity
import com.autocrop.utils.android.debuggingModeEnabled
import com.autocrop.utils.android.restartTransitionAnimation
import com.w2sv.autocrop.R
import com.google.android.gms.ads.MobileAds
import timber.log.Timber


class WelcomeActivity : SystemUiHidingFragmentActivity(R.layout.activity_welcome) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        MobileAds.initialize(this) {}

        if (debuggingModeEnabled())
            Timber.plant(Timber.DebugTree())

        Handler().postDelayed(
            {
                startActivity(
                    Intent(
                        this,
                        MainActivity::class.java
                    )
                )
                restartTransitionAnimation()
                finish()
            },
            1500
        )
    }
}