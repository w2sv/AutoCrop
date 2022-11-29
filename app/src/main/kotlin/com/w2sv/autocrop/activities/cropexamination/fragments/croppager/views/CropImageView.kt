package com.w2sv.autocrop.activities.cropexamination.fragments.croppager.views

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.findFragment
import com.w2sv.androidutils.ActivityRetriever
import com.w2sv.androidutils.extensions.show
import com.w2sv.androidutils.extensions.viewModel
import com.w2sv.autocrop.activities.cropexamination.fragments.croppager.CropPagerFragment
import com.w2sv.autocrop.activities.cropexamination.fragments.croppager.dialogs.CropDialog
import com.w2sv.autocrop.activities.cropexamination.fragments.croppager.dialogs.CropEntiretyDialog

class CropImageView(context: Context, attributeSet: AttributeSet) :
    AppCompatImageView(context, attributeSet),
    ActivityRetriever by ActivityRetriever.Implementation(context) {

    private val viewModel by viewModel<CropPagerFragment.ViewModel>()

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
        viewModel.doAutoScrollLive.value == false
}