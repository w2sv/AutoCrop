package com.w2sv.autocrop.activities.examination.fragments.croppager.views

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.findFragment
import com.w2sv.androidutils.extensions.show
import com.w2sv.androidutils.extensions.viewModel
import com.w2sv.autocrop.activities.examination.fragments.croppager.CropPagerFragment
import com.w2sv.autocrop.activities.examination.fragments.croppager.dialogs.SaveAllCropsDialog
import com.w2sv.autocrop.activities.examination.fragments.croppager.dialogs.SaveCropDialog

class CropImageView(context: Context, attributeSet: AttributeSet) :
    AppCompatImageView(context, attributeSet) {

    private val viewModel by viewModel<CropPagerFragment.ViewModel>()

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        setOnClickListener {
            if (viewModel.dialogInflationEnabled)
                showDialogFragment(viewModel.getCropDialog())
        }

        setOnLongClickListener {
            if (viewModel.dialogInflationEnabled) {
                showDialogFragment(
                    if (viewModel.singleCropRemaining)
                        viewModel.getCropDialog()
                    else
                        SaveAllCropsDialog.getInstance(viewModel.dataSet.size)
                )
                true
            }
            else
                false
        }
    }

    private fun showDialogFragment(fragment: DialogFragment) {
        fragment.show(findFragment<CropPagerFragment>().childFragmentManager)
    }

    private fun CropPagerFragment.ViewModel.getCropDialog(): SaveCropDialog =
        SaveCropDialog
            .getInstance(dataSet.livePosition.value!!)
}