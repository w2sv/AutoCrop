package com.w2sv.autocrop

import com.w2sv.androidutils.DebugTreeCultivatingApplication
import com.w2sv.permissionhandler.PermissionHandler
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
class Application : DebugTreeCultivatingApplication() {

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG)
            Timber.plant(Timber.DebugTree())

        PermissionHandler.setRequiredPermissions(this)
    }
}