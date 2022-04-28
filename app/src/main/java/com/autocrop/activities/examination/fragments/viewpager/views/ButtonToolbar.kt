package com.autocrop.activities.examination.fragments.viewpager.views

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.Toolbar
import androidx.core.view.children
import com.autocrop.activities.examination.ExaminationActivity
import com.autocrop.activities.examination.fragments.saveall.SaveAllFragment
import com.autocrop.activities.examination.fragments.viewpager.ViewPagerViewModel
import com.autocrop.activities.examination.fragments.viewpager.dialogs.CropEntiretyProcedureDialog
import com.autocrop.activities.examination.fragments.viewpager.dialogs.DiscardAllConfirmationDialog
import com.autocrop.activities.examination.fragments.viewpager.dialogs.SaveAllConfirmationDialog
import com.autocrop.uielements.view.ViewModelRetriever

class ButtonToolbar(context: Context, attr: AttributeSet):
    Toolbar(context, attr),
    ViewModelRetriever<ViewPagerViewModel> by ViewPagerViewModelRetriever(context){

    override fun onVisibilityChanged(changedView: View, visibility: Int) {
        super.onVisibilityChanged(changedView, visibility)

        children.forEach {
            it.setOnClickListener(null)
        }
    }
}

abstract class CropEntiretyRegardingButton(context: Context, attr: AttributeSet, private val dialogClass: CropEntiretyProcedureDialog):
    AppCompatButton(context, attr){

    init {
        examinationActivity.supportFragmentManager.setFragmentResultListener(
            dialogClass.resultKey,
            examinationActivity
        )
        {_, _ -> dialogResultListener()}
    }

    override fun setOnClickListener(l: OnClickListener?) {
        super.setOnClickListener{
            dialogClass
                .show(examinationActivity.supportFragmentManager)
        }
    }

    protected abstract fun dialogResultListener()

    protected val examinationActivity: ExaminationActivity
        get() = (context as ExaminationActivity)
}

class DiscardAllButton(context: Context, attr: AttributeSet):
    CropEntiretyRegardingButton(context, attr, DiscardAllConfirmationDialog()){

    override fun dialogResultListener(){
        examinationActivity.invokeSubsequentFragment()
    }
}

class SaveAllButton(context: Context, attr: AttributeSet):
    CropEntiretyRegardingButton(context, attr, SaveAllConfirmationDialog()){

    override fun dialogResultListener() =
        examinationActivity.replaceCurrentFragmentWith(SaveAllFragment(), true)
}