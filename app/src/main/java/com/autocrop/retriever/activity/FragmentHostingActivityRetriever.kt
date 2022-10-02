package com.autocrop.retriever.activity

import com.autocrop.ui.controller.activity.FragmentHostingActivity

interface FragmentHostingActivityRetriever{
    val fragmentHostingActivity: FragmentHostingActivity<*>
}