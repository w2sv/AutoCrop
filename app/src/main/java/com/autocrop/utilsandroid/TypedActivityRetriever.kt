package com.autocrop.utilsandroid

import android.app.Activity

interface TypedActivityRetriever<A: Activity> {
    val typedActivity: A
}