package com.autocrop.application

import android.app.Application
import com.autocrop.global.userPreferencesInstances
import com.autocrop.utils.android.getSharedPreferences
import com.w2sv.autocrop.BuildConfig
import timber.log.Timber

class AutoCrop: Application() {

    /**
     * Initializes [userPreferencesInstances] from application-wide SharedPreferences
     */
    override fun onCreate() {
        super.onCreate()

        // enable Timber if debugging mode enabled
        if (BuildConfig.DEBUG)
            Timber.plant(Timber.DebugTree())

        with(getSharedPreferences()){
            Timber.i("Retrieved SharedPreferences content: $all")

            userPreferencesInstances.forEach {
                it.initializeFromSharedPreferences(this)
            }
        }
    }
}