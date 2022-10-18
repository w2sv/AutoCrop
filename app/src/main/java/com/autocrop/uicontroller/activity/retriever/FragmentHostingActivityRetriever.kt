package com.autocrop.uicontroller.activity.retriever

import com.autocrop.uicontroller.activity.FragmentHostingActivity

interface FragmentHostingActivityRetriever{
    val fragmentHostingActivity: FragmentHostingActivity<*>
}