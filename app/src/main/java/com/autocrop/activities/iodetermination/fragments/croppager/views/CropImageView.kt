package com.autocrop.activities.iodetermination.fragments.croppager.views

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.os.bundleOf
import com.autocrop.activities.iodetermination.IODeterminationActivity
import com.autocrop.activities.iodetermination.fragments.croppager.dialogs.CropDialog
import com.autocrop.activities.iodetermination.fragments.croppager.dialogs.CropEntiretyDialog
import com.autocrop.activities.iodetermination.fragments.croppager.viewmodel.CropPagerViewModel
import com.autocrop.retriever.activity.ActivityRetriever
import com.autocrop.retriever.activity.ContextBasedActivityRetriever
import com.autocrop.utils.android.extensions.activityViewModelLazy

class CropImageView(context: Context, attributeSet: AttributeSet):
    AppCompatImageView(context, attributeSet),
    ActivityRetriever<IODeterminationActivity> by ContextBasedActivityRetriever(context) {

    private val viewModel by activityViewModelLazy<CropPagerViewModel>()

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        setCurrentCropDialog()
        setAllCropsDialog()
    }

    private val dialogInflationEnabled: Boolean
        get() = !viewModel.autoScroll.value!!

    private fun setCurrentCropDialog(){
        setOnClickListener {
            if (dialogInflationEnabled)
                CropDialog().apply {
                    arguments = bundleOf(
                        CropDialog.DATA_SET_POSITION_BUNDLE_ARG_KEY to viewModel.dataSet.currentPosition.value!!
                    )
                }
                    .show(fragmentActivity.supportFragmentManager)
        }
    }

    private fun setAllCropsDialog(){
        setOnLongClickListener {
            if (!dialogInflationEnabled)
                false
            else if (viewModel.dataSet.size == 1)
                performClick()
            else{
                CropEntiretyDialog().show(fragmentActivity.supportFragmentManager)
                true
            }
        }
    }
}