package com.autocrop.utils.android

import android.app.Activity
import android.content.Intent
import android.net.Uri

fun Activity.goToWebpage(url: String){
    startActivity(
        Intent(
            "android.intent.action.VIEW",
            Uri.parse(url)
        )
    )
}