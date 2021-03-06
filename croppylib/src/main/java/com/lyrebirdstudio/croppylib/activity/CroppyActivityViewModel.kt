package com.lyrebirdstudio.croppylib.activity

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel

class CroppyActivityViewModel(val app: Application) : AndroidViewModel(app) {

    var exitActivityAnimation: ((Context) -> Unit)? = null
}