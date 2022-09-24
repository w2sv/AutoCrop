package com.autocrop.retriever.activity

import android.app.Activity
import androidx.fragment.app.FragmentActivity

interface ActivityRetriever<A: Activity>
    : CustomActivityRetriever<A> {

    val activity: Activity
    val fragmentActivity: FragmentActivity
}