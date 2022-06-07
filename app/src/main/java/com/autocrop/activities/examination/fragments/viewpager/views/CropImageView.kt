package com.autocrop.activities.examination.fragments.viewpager.views

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.os.bundleOf
import androidx.lifecycle.LifecycleOwner
import com.autocrop.activities.examination.ExaminationActivity
import com.autocrop.activities.examination.fragments.saveall.SaveAllFragment
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

    private fun setCurrentCropDialog(){
        setOnClickListener {
            if (sharedViewModel.autoScroll.value == false)
                CurrentCropDialog().apply {
                    arguments = bundleOf(
                        CurrentCropDialog.DATA_SET_POSITION_IN to sharedViewModel.dataSet.currentPosition.value!!
                    )
                }
                    .show(fragmentActivity.supportFragmentManager)
        }
    }

    private fun setAllCropsDialog(){
        setAllCropsDialogFragmentResultListeners()
        setOnLongClickListener {
            if (sharedViewModel.dataSet.size == 1)
                performClick()
            else{
                if (sharedViewModel.autoScroll.value == false){
                    AllCropsDialog().show(fragmentActivity.supportFragmentManager)
                    true
                }
                else
                    false
            }
        }
    }

    private fun setAllCropsDialogFragmentResultListeners(){
        mapOf(
            AllCropsDialog.SAVE_ALL_FRAGMENT_RESULT_KEY to {
                typedActivity.replaceCurrentFragmentWith(
                    SaveAllFragment(),
                    true
                )
            },
            AllCropsDialog.DISCARD_ALL_FRAGMENT_RESULT_KEY to {
                typedActivity.returnToMainActivity()
            }
        ).entries.forEach { (key, fragmentResultListener) ->
            fragmentActivity.supportFragmentManager.setFragmentResultListener(
                key,
                activity as LifecycleOwner
            ){_, _ -> fragmentResultListener()}
        }
    }
}