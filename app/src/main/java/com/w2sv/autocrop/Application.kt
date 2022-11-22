package com.w2sv.autocrop

import android.app.Application
import com.w2sv.permissionhandler.PermissionHandler
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
class Application : Application() {

    /**
     * Plants [Timber] tree if debug mode active
     */
    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG)
            Timber.plant(Timber.DebugTree())

        PermissionHandler.setRequiredPermissions(this)
    }
}