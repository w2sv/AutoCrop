package com.autocrop.uielements.view

import android.app.Activity
import android.content.Context
import androidx.fragment.app.FragmentActivity
import com.autocrop.utilsandroid.TypedActivityRetriever

interface ParentActivityRetriever<A: Activity>
    : TypedActivityRetriever<A> {

    val activity: Activity
    val fragmentActivity: FragmentActivity
}

@Suppress("UNCHECKED_CAST")
class ParentActivityRetrievingView<A: Activity>(private val context: Context)
    : ParentActivityRetriever<A> {

    override val activity: Activity
        get() = context as Activity

    override val fragmentActivity: FragmentActivity
        get() = context as FragmentActivity

    override val typedActivity: A
        get() = context as A
}