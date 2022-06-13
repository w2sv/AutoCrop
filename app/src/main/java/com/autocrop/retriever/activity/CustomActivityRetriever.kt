package com.autocrop.retriever.activity

import android.app.Activity
import com.autocrop.uicontroller.activity.FragmentHostingActivity

interface CustomActivityRetriever<A: Activity> {
    val fragmentHostingActivity: FragmentHostingActivity<*>
    val typedActivity: A
}