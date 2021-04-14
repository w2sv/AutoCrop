package com.autocrop


object GlobalParameters {
    var deleteInputScreenshots: Boolean? = null
    var saveDirectoryPath: String? = null

    fun toggleDeleteInputScreenshots(){
        deleteInputScreenshots = !deleteInputScreenshots!!
    }
}