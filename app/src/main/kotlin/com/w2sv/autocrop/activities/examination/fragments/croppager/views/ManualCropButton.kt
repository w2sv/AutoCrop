package com.w2sv.autocrop.activities.examination.fragments.croppager.views

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageButton
import com.w2sv.androidutils.extensions.viewModel
import com.w2sv.autocrop.activities.FragmentedActivity
import com.w2sv.autocrop.activities.examination.fragments.croppager.CropPagerFragment
import com.w2sv.autocrop.activities.examination.fragments.manualcrop.ManualCropFragment

class ManualCropButton(context: Context, attributeSet: AttributeSet) :
    AppCompatImageButton(context, attributeSet),
    FragmentedActivity.Retriever by FragmentedActivity.Retriever.Implementation(context) {

    private val viewModel by viewModel<CropPagerFragment.ViewModel>()

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        if (!isInEditMode) {
            setOnClickListener {
                launchManualCropFragment()
            }
        }
    }

    private fun launchManualCropFragment() {
        fragmentedActivity.fragmentReplacementTransaction(
            ManualCropFragment.instance(
                viewModel.dataSet.liveElement
            ),
            true
        )
            .addToBackStack(null)
            .commit()
    }
}