package com.autocrop.activities.iodetermination.fragments.croppager.views

import android.content.Context
import android.util.AttributeSet
import com.autocrop.activities.iodetermination.IODeterminationActivity
import com.autocrop.activities.iodetermination.fragments.croppager.viewmodel.CropPagerViewModel
import com.autocrop.activities.iodetermination.fragments.manualcrop.ManualCropFragment
import com.autocrop.uicontroller.activity.retriever.ActivityRetriever
import com.autocrop.uicontroller.activity.retriever.ContextBasedActivityRetriever
import com.autocrop.utils.android.extensions.viewModel
import com.autocrop.views.ExtendedAppCompatImageButton
import com.w2sv.autocrop.R

class ManualCropButton(context: Context, attributeSet: AttributeSet) :
    ExtendedAppCompatImageButton(context, attributeSet),
    ActivityRetriever<IODeterminationActivity> by ContextBasedActivityRetriever(context) {

    override fun onClickListener() {
        fragmentHostingActivity.fragmentReplacementTransaction(
            ManualCropFragment.instance(
                viewModel<CropPagerViewModel>().dataSet.currentValue
            ),
            R.anim.animate_in_out_enter,
            R.anim.animate_in_out_exit
        )
            .addToBackStack(null)
            .commit()
    }
}