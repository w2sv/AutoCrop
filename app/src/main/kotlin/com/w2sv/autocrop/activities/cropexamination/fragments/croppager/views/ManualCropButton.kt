package com.w2sv.autocrop.activities.cropexamination.fragments.croppager.views

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageButton
import com.w2sv.androidutils.extensions.ifNotInEditMode
import com.w2sv.androidutils.extensions.viewModel
import com.w2sv.autocrop.activities.cropexamination.fragments.croppager.viewmodel.CropPagerViewModel
import com.w2sv.autocrop.activities.cropexamination.fragments.manualcrop.ManualCropFragment
import com.w2sv.autocrop.controller.activity.FragmentHostingActivity

class ManualCropButton(context: Context, attributeSet: AttributeSet) :
    AppCompatImageButton(context, attributeSet),
    FragmentHostingActivity.Retriever by FragmentHostingActivity.Retriever.Implementation(context) {

    private val viewModel by viewModel<CropPagerViewModel>()

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        ifNotInEditMode {
            setOnClickListener {
                launchManualCropFragment()
            }
        }
    }

    private fun launchManualCropFragment() {
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