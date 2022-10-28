package com.w2sv.autocrop.controller.activity.retriever

import android.app.Activity
import android.content.Context
import androidx.fragment.app.FragmentActivity
import com.w2sv.autocrop.controller.activity.FragmentHostingActivity
import com.w2sv.autocrop.utils.android.extensions.getActivity

class ContextBasedActivityRetriever<A : Activity>(private val context: Context) : ActivityRetriever<A> {

    override val activity: Activity by lazy {
        context.getActivity()!!
    }

    override val fragmentActivity: FragmentActivity
        get() = activity as FragmentActivity

    override val fragmentHostingActivity: FragmentHostingActivity<*>
        get() = activity as FragmentHostingActivity<*>

    @Suppress("UNCHECKED_CAST")
    override val typedActivity: A
        get() = activity as A
}