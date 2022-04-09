package com.autocrop.application

import android.app.Application
import com.autocrop.global.userPreferencesInstances
import com.autocrop.utils.android.getSharedPreferences
import timber.log.Timber

class AutoCrop: Application() {

    /**
     * Initializes [userPreferencesInstances] from application-wide SharedPreferences
     */
    override fun onCreate() {
        super.onCreate()

        with(getSharedPreferences()){
            Timber.i("Retrieved SharedPreferences content: $all")

            userPreferencesInstances.forEach {
                it.initializeFromSharedPreferences(this)
            }
        }
    }
}