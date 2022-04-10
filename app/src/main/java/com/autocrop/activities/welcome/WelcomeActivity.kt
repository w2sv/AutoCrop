package com.autocrop.activities.welcome

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.autocrop.activities.ActivityTransitions
import com.autocrop.activities.main.MainActivity

class WelcomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        startActivity(
            Intent(
                this,
                MainActivity::class.java
            )
        )
        ActivityTransitions.RESTART(this)

        // transition to main activity after certain delay
//        Handler(Looper.getMainLooper()).postDelayed(
//            {
//                startActivity(
//                    Intent(
//                        this,
//                        MainActivity::class.java
//                    )
//                )
//                ActivityTransitions.RESTART(this)
//            },
//            700
//        )
    }
}