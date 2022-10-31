package com.w2sv.autocrop.activities.iodetermination.fragments.croppager.views

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.findFragment
import com.w2sv.autocrop.activities.iodetermination.fragments.croppager.CropPagerFragment
import com.w2sv.autocrop.activities.iodetermination.fragments.croppager.dialogs.CropDialog
import com.w2sv.autocrop.activities.iodetermination.fragments.croppager.dialogs.CropEntiretyDialog
import com.w2sv.autocrop.activities.iodetermination.fragments.croppager.viewmodel.CropPagerViewModel
import com.w2sv.autocrop.controller.activity.retriever.ActivityRetriever
import com.w2sv.autocrop.controller.activity.retriever.ContextBasedActivityRetriever
import com.w2sv.autocrop.utils.android.extensions.show
import com.w2sv.autocrop.utils.android.extensions.viewModelLazy

class CropImageView(context: Context, attributeSet: AttributeSet) :
    AppCompatImageView(context, attributeSet),
    ActivityRetriever by ContextBasedActivityRetriever(context) {

    private val viewModel by viewModelLazy<CropPagerViewModel>()

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        setOnClickListener {
            if (dialogInflationEnabled())
                showDialog(cropDialog())
        }

        setOnLongClickListener {
            if (dialogInflationEnabled()) {
                showDialog(
                    if (viewModel.dataSet.size == 1)
                        cropDialog()
                    else
                        CropEntiretyDialog()
                )
                true
            }
            else
                false
        }
    }

    private fun cropDialog(): CropDialog =
        CropDialog
            .instance(viewModel.dataSet.livePosition.value!!)

    private fun showDialog(dialog: DialogFragment) {
        dialog.show(findFragment<CropPagerFragment>().childFragmentManager)
    }

    private fun dialogInflationEnabled(): Boolean =
        viewModel.liveAutoScroll.value == false
}