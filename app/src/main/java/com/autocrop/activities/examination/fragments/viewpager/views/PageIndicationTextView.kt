package com.autocrop.activities.examination.fragments.viewpager.views

import android.content.Context
import android.util.AttributeSet
import com.w2sv.autocrop.R

class PageIndicationTextView(context: Context, attr: AttributeSet): PageDependentTextView(context, attr) {
    override fun update(position: Int){
        text = template.format(position, sharedViewModel.dataSet.size)
    }
}