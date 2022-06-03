package com.autocrop.activities.examination.fragments.viewpager.views

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.os.bundleOf
import com.autocrop.activities.examination.ExaminationActivity
import com.autocrop.activities.examination.fragments.viewpager.ViewPagerViewModel
import com.autocrop.activities.examination.fragments.viewpager.dialogs.SingleCropProcedureDialog
import com.autocrop.retriever.activity.ActivityRetriever
import com.autocrop.retriever.activity.ContextBasedActivityRetriever
import com.autocrop.retriever.viewmodel.ViewModelRetriever
import com.autocrop.uielements.view.ImmersiveViewOnTouchListener

class CropImageView(context: Context, attributeSet: AttributeSet):
    AppCompatImageView(context, attributeSet),
    ViewModelRetriever<ViewPagerViewModel> by ViewPagerViewModelRetriever(context),
    ActivityRetriever<ExaminationActivity> by ContextBasedActivityRetriever(context) {

    init {
        setOnTouchListener(
            object : ImmersiveViewOnTouchListener() {

                /**
                 * Cancel scroller upon touch if running
                 */
                @SuppressLint("ClickableViewAccessibility")
                override fun onTouch(v: View?, event: MotionEvent?): Boolean =
                    if (sharedViewModel.autoScroll.value == true)
                        false
                    else
                        super.onTouch(v, event)

                private val cropProcedureDialog: SingleCropProcedureDialog by lazy{
                    SingleCropProcedureDialog()
                }

                /**
                 * Invoke CropProcedureDialog
                 */
                override fun onClick() = with(cropProcedureDialog) {
                    arguments = bundleOf(
                        SingleCropProcedureDialog.DATA_SET_POSITION_IN to sharedViewModel.dataSet.position.value!!
                    )
                    show(fragmentActivity.supportFragmentManager)
                }
            }
        )
    }
}