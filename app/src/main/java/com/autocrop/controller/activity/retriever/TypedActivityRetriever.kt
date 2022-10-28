package com.autocrop.controller.activity.retriever

import android.app.Activity

interface TypedActivityRetriever<A : Activity> {
    val typedActivity: A
}