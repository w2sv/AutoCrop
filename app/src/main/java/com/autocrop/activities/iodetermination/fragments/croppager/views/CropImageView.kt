package com.autocrop.activities.iodetermination.fragments.croppager.views

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.os.bundleOf
import com.autocrop.activities.iodetermination.IODeterminationActivity
import com.autocrop.activities.iodetermination.fragments.croppager.dialogs.CropEntiretyDialog
import com.autocrop.activities.iodetermination.fragments.croppager.dialogs.CropDialog
import com.autocrop.activities.iodetermination.fragments.croppager.viewmodel.ViewPagerViewModel
import com.autocrop.retriever.activity.ActivityRetriever
import com.autocrop.retriever.activity.ContextBasedActivityRetriever
import com.autocrop.retriever.viewmodel.ViewModelRetriever

class CropImageView(context: Context, attributeSet: AttributeSet):
    AppCompatImageView(context, attributeSet),
    ViewModelRetriever<ViewPagerViewModel> by ViewPagerViewModelRetriever(context),
    ActivityRetriever<IODeterminationActivity> by ContextBasedActivityRetriever(context) {

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        setCurrentCropDialog()
        setAllCropsDialog()
    }

    private val dialogInflationEnabled: Boolean
        get() = sharedViewModel.autoScroll.value == false

    private fun setCurrentCropDialog(){
        setOnClickListener {
            if (dialogInflationEnabled)
                CropDialog().apply {
                    arguments = bundleOf(
                        CropDialog.DATA_SET_POSITION_BUNDLE_ARG_KEY to this@CropImageView.sharedViewModel.dataSet.currentPosition.value!!
                    )
                }
                    .show(fragmentActivity.supportFragmentManager)
        }
    }

    private fun setAllCropsDialog(){
        setOnLongClickListener {
            if (!dialogInflationEnabled)
                false
            else if (sharedViewModel.dataSet.size == 1)
                performClick()
            else{
                CropEntiretyDialog().show(fragmentActivity.supportFragmentManager)
                true
            }
        }
    }
}