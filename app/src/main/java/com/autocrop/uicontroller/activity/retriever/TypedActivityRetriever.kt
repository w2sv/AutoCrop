package com.autocrop.uicontroller.activity.retriever

import android.app.Activity

interface TypedActivityRetriever<A: Activity> {
    val typedActivity: A
}