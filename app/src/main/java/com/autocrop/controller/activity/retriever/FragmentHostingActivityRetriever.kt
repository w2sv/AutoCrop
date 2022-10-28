package com.autocrop.controller.activity.retriever

import com.autocrop.controller.activity.FragmentHostingActivity

interface FragmentHostingActivityRetriever {
    val fragmentHostingActivity: FragmentHostingActivity<*>
}