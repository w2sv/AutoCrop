package com.autocrop.retriever.activity

import android.app.Activity
import android.content.Context
import androidx.fragment.app.FragmentActivity
import com.autocrop.uicontroller.activity.FragmentHostingActivity

@Suppress("UNCHECKED_CAST")
class ContextBasedActivityRetriever<A: Activity>(private val context: Context)
    : ActivityRetriever<A> {

    override val activity: Activity
        get() = context as Activity

    override val fragmentActivity: FragmentActivity
        get() = context as FragmentActivity

    override val fragmentHostingActivity: FragmentHostingActivity<*>
        get() = context as FragmentHostingActivity<*>

    override val typedActivity: A
        get() = context as A
}