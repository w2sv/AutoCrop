package com.autocrop.retriever.activity

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.fragment.app.FragmentActivity
import com.autocrop.ui.controller.activity.FragmentHostingActivity

class ContextBasedActivityRetriever<A: Activity>(private val context: Context)
    : ActivityRetriever<A> {

    override val activity: Activity by lazy {
        context.getActivity()!!
    }

    private tailrec fun Context.getActivity(): Activity? =
        this as? Activity ?: (this as? ContextWrapper)?.baseContext?.getActivity()

    override val fragmentActivity: FragmentActivity
        get() = activity as FragmentActivity

    override val fragmentHostingActivity: FragmentHostingActivity<*>
        get() = activity as FragmentHostingActivity<*>

    @Suppress("UNCHECKED_CAST")
    override val typedActivity: A
        get() = activity as A
}