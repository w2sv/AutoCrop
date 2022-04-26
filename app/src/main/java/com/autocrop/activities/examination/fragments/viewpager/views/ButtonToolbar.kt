package com.autocrop.activities.examination.fragments.viewpager.views

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.Toolbar
import com.autocrop.activities.examination.fragments.viewpager.ViewPagerFragmentViewModel
import com.autocrop.uielements.view.ViewModelRetriever
import com.autocrop.uielements.view.show

class ButtonToolbar(context: Context, attr: AttributeSet):
    Toolbar(context, attr),
    ViewModelRetriever<ViewPagerFragmentViewModel> by ViewPagerViewModelRetriever(context){

    init {
        if (!viewModel.autoScroll)
            show()
    }

}