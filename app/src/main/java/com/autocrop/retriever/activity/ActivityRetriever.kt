package com.autocrop.retriever.activity

import android.app.Activity
import androidx.fragment.app.FragmentActivity
import com.autocrop.uicontroller.activity.FragmentHostingActivity

interface ActivityRetriever<A: Activity>
    : CustomActivityRetriever<A> {

    val activity: Activity
    val fragmentActivity: FragmentActivity
}