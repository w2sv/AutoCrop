package com.autocrop.utils.android

import android.net.Uri
import java.io.File

val Uri.fileName: String
    get() = File(path!!).name