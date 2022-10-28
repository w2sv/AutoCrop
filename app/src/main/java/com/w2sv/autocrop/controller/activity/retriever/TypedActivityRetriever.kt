package com.w2sv.autocrop.controller.activity.retriever

import android.app.Activity

interface TypedActivityRetriever<A : Activity> {
    val typedActivity: A
}