package com.w2sv.autocrop.controller.activity.retriever

import com.w2sv.autocrop.controller.activity.FragmentHostingActivity

interface FragmentHostingActivityRetriever {
    val fragmentHostingActivity: FragmentHostingActivity<*>
}