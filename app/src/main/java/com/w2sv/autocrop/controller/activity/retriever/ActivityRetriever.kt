package com.w2sv.autocrop.controller.activity.retriever

import android.app.Activity
import androidx.fragment.app.FragmentActivity

interface ActivityRetriever: FragmentHostingActivityRetriever {
    val activity: Activity
    val fragmentActivity: FragmentActivity

    @Suppress("UNCHECKED_CAST")
    fun <A: Activity> castActivity(): A =
        activity as A
}