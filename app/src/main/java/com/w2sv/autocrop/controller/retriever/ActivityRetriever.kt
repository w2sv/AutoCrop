package com.w2sv.autocrop.controller.retriever

import android.app.Activity
import android.content.Context
import androidx.fragment.app.FragmentActivity
import com.w2sv.autocrop.controller.activity.FragmentHostingActivity
import com.w2sv.autocrop.utils.android.extensions.getActivity

interface ActivityRetriever : FragmentHostingActivity.Retriever {
    val activity: Activity
    val fragmentActivity: FragmentActivity

    @Suppress("UNCHECKED_CAST")
    fun <A : Activity> castActivity(): A =
        activity as A

    class Implementation(private val context: Context) : ActivityRetriever {

        override val activity: Activity by lazy {
            context.getActivity()!!
        }

        override val fragmentActivity: FragmentActivity
            get() = activity as FragmentActivity

        override val fragmentHostingActivity: FragmentHostingActivity
            get() = activity as FragmentHostingActivity
    }
}