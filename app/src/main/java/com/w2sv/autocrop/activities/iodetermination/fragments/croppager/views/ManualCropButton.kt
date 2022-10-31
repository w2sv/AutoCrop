package com.w2sv.autocrop.activities.iodetermination.fragments.croppager.views

import android.content.Context
import android.util.AttributeSet
import com.w2sv.autocrop.activities.iodetermination.fragments.croppager.viewmodel.CropPagerViewModel
import com.w2sv.autocrop.activities.iodetermination.fragments.manualcrop.ManualCropFragment
import com.w2sv.autocrop.controller.activity.retriever.ActivityRetriever
import com.w2sv.autocrop.controller.activity.retriever.ContextBasedActivityRetriever
import com.w2sv.autocrop.ui.views.ExtendedAppCompatImageButton
import com.w2sv.autocrop.utils.android.extensions.viewModelLazy

class ManualCropButton(context: Context, attributeSet: AttributeSet) :
    ExtendedAppCompatImageButton(context, attributeSet),
    ActivityRetriever by ContextBasedActivityRetriever(context) {

    private val viewModel by viewModelLazy<CropPagerViewModel>()

    override fun onClickListener() {
        fragmentHostingActivity.fragmentReplacementTransaction(
            ManualCropFragment.instance(
                viewModel.dataSet.liveElement
            ),
            true
        )
            .addToBackStack(null)
            .commit()
    }
}