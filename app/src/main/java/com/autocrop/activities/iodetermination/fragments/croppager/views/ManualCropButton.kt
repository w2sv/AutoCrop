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
import com.blogspot.atifsoftwares.animatoolib.Animatoo
import com.w2sv.autocrop.R

class ManualCropButton(context: Context, attributeSet: AttributeSet) :
    ExtendedAppCompatImageButton(context, attributeSet),
    ActivityRetriever<IODeterminationActivity> by ContextBasedActivityRetriever(context) {

    override fun onClickListener() {
        fragmentHostingActivity.fragmentReplacementTransaction(
            ManualCropFragment.instance(
                cropBundleIndex = viewModel<CropPagerViewModel>().dataSet.currentPosition.value!!
            ),
            R.animator.in_out_enter,
            R.animator.in_out_exit
        )
            .addToBackStack(null)
            .commit()

        Animatoo.animateInAndOut(context)
    }
}