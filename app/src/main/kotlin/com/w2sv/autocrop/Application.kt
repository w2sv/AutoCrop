package com.w2sv.autocrop

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import org.opencv.android.OpenCVLoader
import timber.log.Timber

@HiltAndroidApp
class Application : Application() {

    override fun onCreate() {
        super.onCreate()

        OpenCVLoader.initDebug()

        if (BuildConfig.DEBUG)
            Timber.plant(Timber.DebugTree())
    }
}