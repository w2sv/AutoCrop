package com.autocrop.application

import android.app.Application
import com.autocrop.global.preferencesInstances
import com.autocrop.utilsandroid.getApplicationWideSharedPreferences
import com.w2sv.autocrop.BuildConfig
import timber.log.Timber

class AutoCrop: Application() {

    /**
     * Plants [Timber] tree if applicable
     * initializes [preferencesInstances] from application-wide SharedPreferences
     */
    override fun onCreate() {
        super.onCreate()

        // enable Timber if debugging mode enabled
        if (BuildConfig.DEBUG)
            Timber.plant(Timber.DebugTree())

        // initialize userPreferencesInstances
        with(getApplicationWideSharedPreferences()){
            Timber.i("Retrieved SharedPreferences content: $all")

            preferencesInstances.forEach {
                it.initializeFromSharedPreferences(this)
            }
        }
    }
}