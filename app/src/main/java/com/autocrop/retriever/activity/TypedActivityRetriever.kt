package com.autocrop.retriever.activity

import android.app.Activity

interface TypedActivityRetriever<A: Activity> {
    val typedActivity: A
}