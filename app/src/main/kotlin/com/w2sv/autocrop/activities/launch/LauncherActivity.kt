package com.w2sv.autocrop.activities.launch

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.w2sv.autocrop.activities.main.MainActivity
import com.w2sv.autocrop.activities.onboarding.OnboardingActivity
import com.w2sv.preferences.GlobalFlags
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class LauncherActivity : AppCompatActivity() {

    @Inject
    lateinit var globalFlags: GlobalFlags

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()

        super.onCreate(savedInstanceState)

        startActivity(
            Intent(
                this,
                if (globalFlags.onboardingDone)
                    MainActivity::class.java
                else
                    OnboardingActivity::class.java
            )
                .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        )
    }
}