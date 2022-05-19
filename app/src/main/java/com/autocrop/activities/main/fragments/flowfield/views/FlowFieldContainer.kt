package com.autocrop.activities.main.fragments.flowfield.views

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.fragment.app.FragmentActivity
import com.autocrop.activities.main.MainActivity
import com.autocrop.activities.main.fragments.flowfield.sketch.FlowFieldSketch
import com.autocrop.uielements.view.ParentActivityRetriever
import com.autocrop.uielements.view.ParentActivityRetrievingView
import com.autocrop.utilsandroid.screenResolution
import processing.android.PFragment

class FlowFieldContainer(context: Context, attr: AttributeSet) :
    FrameLayout(context, attr),
    ParentActivityRetriever<MainActivity> by ParentActivityRetrievingView(context) {

    init {
        with(screenResolution(activity.windowManager)){
            PFragment(FlowFieldSketch(x, y)).setView(this@FlowFieldContainer, fragmentActivity)
        }
    }
}