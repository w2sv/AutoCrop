package com.autocrop.application

import android.app.Application
import com.autocrop.global.userPreferencesInstances
import com.autocrop.utils.android.debuggingModeEnabled
import com.autocrop.utils.android.getSharedPreferences
import timber.log.Timber

class AutoCrop: Application() {

    /**
     * Initializes [userPreferencesInstances] from application-wide SharedPreferences
     */
    override fun onCreate() {
        super.onCreate()

        // enable Timber if debugging mode enabled
        if (debuggingModeEnabled())
            Timber.plant(Timber.DebugTree())

        with(getSharedPreferences()){
            Timber.i("Retrieved SharedPreferences content: $all")

            userPreferencesInstances.forEach {
                it.initializeFromSharedPreferences(this)
            }
        }
    }
}