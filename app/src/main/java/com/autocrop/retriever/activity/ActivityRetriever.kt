package com.autocrop.retriever.activity

import android.app.Activity
import androidx.fragment.app.FragmentActivity

interface ActivityRetriever<A: Activity>
    : TypedActivityRetriever<A> {

    val activity: Activity
    val fragmentActivity: FragmentActivity
}