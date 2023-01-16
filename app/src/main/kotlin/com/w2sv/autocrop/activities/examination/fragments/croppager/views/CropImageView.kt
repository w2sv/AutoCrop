package com.w2sv.autocrop.activities.examination.fragments.croppager.views

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.findFragment
import com.w2sv.androidutils.extensions.show
import com.w2sv.androidutils.extensions.viewModel
import com.w2sv.autocrop.activities.examination.fragments.croppager.CropPagerFragment
import com.w2sv.autocrop.activities.examination.fragments.croppager.dialogs.CropDialog
import com.w2sv.autocrop.activities.examination.fragments.croppager.dialogs.CropEntiretyDialog

class CropImageView(context: Context, attributeSet: AttributeSet) :
    AppCompatImageView(context, attributeSet) {

    private val viewModel by viewModel<CropPagerFragment.ViewModel>()

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        setOnClickListener {
            if (viewModel.dialogInflationEnabled)
                viewModel.getCropDialog().show()
        }

        setOnLongClickListener {
            if (viewModel.dialogInflationEnabled) {
                (if (viewModel.dataSet.size == 1)
                    viewModel.getCropDialog()
                else
                    CropEntiretyDialog())
                    .show()
                true
            }
            else
                false
        }
    }

    private fun DialogFragment.show() {
        show(findFragment<CropPagerFragment>().childFragmentManager)
    }

    private fun CropPagerFragment.ViewModel.getCropDialog(): CropDialog =
        CropDialog
            .instance(dataSet.livePosition.value!!)

    private val CropPagerFragment.ViewModel.dialogInflationEnabled: Boolean
        get() = doAutoScrollLive.value == false
}