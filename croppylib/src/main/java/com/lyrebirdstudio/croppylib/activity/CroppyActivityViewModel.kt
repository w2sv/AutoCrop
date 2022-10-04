package com.lyrebirdstudio.croppylib.activity

import android.content.Context
import androidx.lifecycle.ViewModel

class CroppyActivityViewModel : ViewModel() {
    var exitAnimation: ((Context) -> Unit)? = null
}