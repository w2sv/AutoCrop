package com.w2sv.autocrop

import android.app.Application
import com.w2sv.autocrop.preferences.BooleanPreferences
import com.w2sv.autocrop.preferences.UriPreferences
import com.w2sv.autocrop.utils.android.extensions.getApplicationWideSharedPreferences
import com.w2sv.permissionhandler.PermissionHandler
import timber.log.Timber

class AutoCrop : Application() {

    /**
     * Plants [Timber] tree if applicable
     * initializes TypedPreferences child classes from application-wide SharedPreferences
     */
    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG)
            Timber.plant(Timber.DebugTree())

        with(getApplicationWideSharedPreferences()) {
            listOf(BooleanPreferences, UriPreferences).forEach {
                it.initializeFromSharedPreferences(this)
            }
        }

        PermissionHandler.setRequiredPermissions(this)
    }
}