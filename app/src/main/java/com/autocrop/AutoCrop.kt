package com.autocrop

import android.app.Application
import com.autocrop.preferences.TypedPreferences
import com.autocrop.utils.android.extensions.getApplicationWideSharedPreferences
import com.w2sv.autocrop.BuildConfig
import timber.log.Timber

class AutoCrop: Application() {

    /**
     * Plants [Timber] tree if applicable
     * initializes [TypedPreferences] child classes from application-wide SharedPreferences
     */
    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG)
            Timber.plant(Timber.DebugTree())

        with(getApplicationWideSharedPreferences()){
            TypedPreferences::class.sealedSubclasses.forEach {
                it.objectInstance!!.initializeFromSharedPreferences(this)
            }
        }
    }
}