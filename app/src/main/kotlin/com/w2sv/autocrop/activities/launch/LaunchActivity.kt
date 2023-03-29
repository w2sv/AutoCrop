package com.w2sv.autocrop.activities.launch

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.w2sv.autocrop.activities.main.MainActivity
import com.w2sv.autocrop.activities.onboarding.OnboardingActivity
import com.w2sv.preferences.GlobalFlags
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@SuppressLint("CustomSplashScreen")
@AndroidEntryPoint
class LaunchActivity : AppCompatActivity() {

    @HiltViewModel
    class ViewModel @Inject constructor(globalFlags: GlobalFlags) : androidx.lifecycle.ViewModel() {
        val destinationActivity = when (globalFlags.onboardingDone) {
            true -> MainActivity::class.java
            false -> OnboardingActivity::class.java
        }
    }

    private val viewModel by viewModels<ViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()

        super.onCreate(savedInstanceState)

        startActivity(
            Intent(
                this,
                viewModel.destinationActivity
            )
                .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        )
    }
}