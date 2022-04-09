package com.autocrop.application

import android.app.Application
import com.autocrop.global.BooleanUserPreferences
import com.autocrop.global.SaveDestinationPreferences
import com.autocrop.utils.android.getSharedPreferences

class AutoCrop: Application() {
    override fun onCreate() {
        super.onCreate()

        val sharedMainActivityPreferences = getSharedPreferences()
        println("SharedPreferences content: ${sharedMainActivityPreferences.all}")

        SaveDestinationPreferences.initializeFromSharedPreferences(sharedMainActivityPreferences)
        BooleanUserPreferences.initializeFromSharedPreferences(sharedMainActivityPreferences)
    }
}