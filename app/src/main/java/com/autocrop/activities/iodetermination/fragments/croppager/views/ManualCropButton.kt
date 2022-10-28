package com.autocrop.activities.iodetermination.fragments.croppager.views

import android.content.Context
import android.util.AttributeSet
import com.autocrop.activities.iodetermination.IODeterminationActivity
import com.autocrop.activities.iodetermination.fragments.croppager.viewmodel.CropPagerViewModel
import com.autocrop.activities.iodetermination.fragments.manualcrop.ManualCropFragment
import com.autocrop.controller.activity.retriever.ActivityRetriever
import com.autocrop.controller.activity.retriever.ContextBasedActivityRetriever
import com.autocrop.ui.views.ExtendedAppCompatImageButton
import com.autocrop.utils.android.extensions.viewModelLazy

class ManualCropButton(context: Context, attributeSet: AttributeSet) :
    ExtendedAppCompatImageButton(context, attributeSet),
    ActivityRetriever<IODeterminationActivity> by ContextBasedActivityRetriever(context) {

    private val viewModel by viewModelLazy<CropPagerViewModel>()

    override fun onClickListener() {
        fragmentHostingActivity.fragmentReplacementTransaction(
            ManualCropFragment.instance(
                viewModel.dataSet.currentElement
            ),
            true
        )
            .addToBackStack(null)
            .commit()
    }
}