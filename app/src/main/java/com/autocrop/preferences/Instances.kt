package com.autocrop.preferences

typealias PreferencesArray = Array<Preferences<*>>

val preferencesInstances: PreferencesArray = arrayOf(BooleanPreferences, UriPreferences)