package com.autocrop.activities.examination.fragments.viewpager.views

import android.content.Context
import android.util.AttributeSet
import com.w2sv.autocrop.R

class PageIndicationTextView(context: Context, attr: AttributeSet): PageDependentTextView(context, attr, R.string.fracture) {
    override fun updateText(position: Int){
        text = stringResource.format(position + 1, sharedViewModel.dataSet.size)
    }
}