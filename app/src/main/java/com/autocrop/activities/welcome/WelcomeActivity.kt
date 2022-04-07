package com.autocrop.activities.welcome

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.FragmentActivity
import com.autocrop.activities.ActivityTransitions
import com.autocrop.activities.main.MainActivity
import com.autocrop.utils.android.debuggingModeEnabled
import com.w2sv.autocrop.R
import timber.log.Timber


class WelcomeActivity : FragmentActivity(R.layout.activity_welcome) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // enable Timber if debugging mode enabled
        if (debuggingModeEnabled())
            Timber.plant(Timber.DebugTree())

        // transition to main activity after certain delay
        Handler(Looper.getMainLooper()).postDelayed(
            {
                startActivity(
                    Intent(
                        this,
                        MainActivity::class.java
                    )
                )
                ActivityTransitions.RESTART(this)
            },
            700
        )
    }

    override fun onStop() {
        super.onStop()

        finishAndRemoveTask()
    }
}