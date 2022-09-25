package com.autocrop.activities.examination.fragments.croppager.views

import android.content.Context
import android.util.AttributeSet

class PageIndicationTextView(context: Context, attr: AttributeSet): PageDependentTextView(context, attr) {
    override fun update(position: Int){
        text = template.format(position, sharedViewModel.dataSet.size)
    }
}