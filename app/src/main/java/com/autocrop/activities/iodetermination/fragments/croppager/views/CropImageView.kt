package com.autocrop.activities.iodetermination.fragments.croppager.views

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.os.bundleOf
import com.autocrop.activities.iodetermination.IODeterminationActivity
import com.autocrop.activities.iodetermination.fragments.croppager.dialogs.CropDialog
import com.autocrop.activities.iodetermination.fragments.croppager.dialogs.CropEntiretyDialog
import com.autocrop.activities.iodetermination.fragments.croppager.viewmodel.CropPagerViewModel
import com.autocrop.controller.activity.retriever.ActivityRetriever
import com.autocrop.controller.activity.retriever.ContextBasedActivityRetriever
import com.autocrop.utils.android.extensions.show
import com.autocrop.utils.android.extensions.viewModelLazy

class CropImageView(context: Context, attributeSet: AttributeSet) :
    AppCompatImageView(context, attributeSet),
    ActivityRetriever<IODeterminationActivity> by ContextBasedActivityRetriever(context) {

    private val viewModel by viewModelLazy<CropPagerViewModel>()

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        setOnClickListener { onClickListener() }
        setOnLongClickListener { onLongClickListener() }
    }

    private fun onClickListener() {
        if (dialogInflationEnabled())
            CropDialog().apply {
                arguments = bundleOf(
                    CropDialog.DATA_SET_POSITION_BUNDLE_ARG_KEY to viewModel.dataSet.currentPosition.value!!
                )
            }
                .show(fragmentActivity.supportFragmentManager)
    }

    private fun onLongClickListener(): Boolean =
        if (!dialogInflationEnabled())
            false
        else if (viewModel.dataSet.size == 1)
            performClick()
        else {
            CropEntiretyDialog().show(fragmentActivity.supportFragmentManager)
            true
        }

    private fun dialogInflationEnabled(): Boolean =
        viewModel.autoScroll.value == false
}