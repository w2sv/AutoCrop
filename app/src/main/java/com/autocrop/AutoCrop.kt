package com.autocrop

import android.app.Application
import com.autocrop.preferences.BooleanPreferences
import com.autocrop.preferences.UriPreferences
import com.autocrop.utils.android.PermissionHandler
import com.autocrop.utils.android.extensions.getApplicationWideSharedPreferences
import com.w2sv.autocrop.BuildConfig
import timber.log.Timber

class AutoCrop: Application() {

    /**
     * Plants [Timber] tree if applicable
     * initializes TypedPreferences child classes from application-wide SharedPreferences
     */
    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG)
            Timber.plant(Timber.DebugTree())

        with(getApplicationWideSharedPreferences()){
            listOf(BooleanPreferences, UriPreferences).forEach {
                it.initializeFromSharedPreferences(this)
            }
        }

        PermissionHandler.setRequiredPermissions(this)
    }
}