package com.autocrop.activities.examination.fragments.viewpager.views

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.os.bundleOf
import com.autocrop.activities.examination.ExaminationActivity
import com.autocrop.activities.examination.fragments.viewpager.ViewPagerViewModel
import com.autocrop.activities.examination.fragments.viewpager.dialogs.AllCropsDialog
import com.autocrop.activities.examination.fragments.viewpager.dialogs.CurrentCropDialog
import com.autocrop.retriever.activity.ActivityRetriever
import com.autocrop.retriever.activity.ContextBasedActivityRetriever
import com.autocrop.retriever.viewmodel.ViewModelRetriever

class CropImageView(context: Context, attributeSet: AttributeSet):
    AppCompatImageView(context, attributeSet),
    ViewModelRetriever<ViewPagerViewModel> by ViewPagerViewModelRetriever(context),
    ActivityRetriever<ExaminationActivity> by ContextBasedActivityRetriever(context) {

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        setCurrentCropDialog()
        setAllCropsDialog()
    }

    private val dialogInflationEnabled: Boolean
        get() = sharedViewModel.autoScroll.value == true

    private fun setCurrentCropDialog(){
        setOnClickListener {
            if (dialogInflationEnabled)
                CurrentCropDialog().apply {
                    arguments = bundleOf(
                        CurrentCropDialog.DATA_SET_POSITION_ARG_KEY to this@CropImageView.sharedViewModel.dataSet.currentPosition.value!!
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
                AllCropsDialog().show(fragmentActivity.supportFragmentManager)
                true
            }
        }
    }
}