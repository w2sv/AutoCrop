package com.autocrop.activities.examination.fragments.viewpager.views

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.Toolbar
import androidx.core.view.children
import androidx.lifecycle.LifecycleOwner
import com.autocrop.activities.examination.ExaminationActivity
import com.autocrop.activities.examination.fragments.saveall.SaveAllFragment
import com.autocrop.activities.examination.fragments.viewpager.dialogs.CropEntiretyProcedureDialog
import com.autocrop.activities.examination.fragments.viewpager.dialogs.DiscardAllConfirmationDialog
import com.autocrop.activities.examination.fragments.viewpager.dialogs.SaveAllConfirmationDialog
import com.autocrop.retriever.activity.ActivityRetriever
import com.autocrop.retriever.activity.ContextBasedActivityRetriever

class ButtonToolbar(context: Context, attr: AttributeSet):
    Toolbar(context, attr){

    override fun onVisibilityChanged(changedView: View, visibility: Int) {
        super.onVisibilityChanged(changedView, visibility)

        children.forEach {
            it.setOnClickListener(null)
        }
    }
}

abstract class CropEntiretyRegardingButton(context: Context, attr: AttributeSet, private val dialogClass: CropEntiretyProcedureDialog):
    AppCompatButton(context, attr),
    ActivityRetriever<ExaminationActivity> by ContextBasedActivityRetriever(context) {

    init {
        fragmentActivity.supportFragmentManager.setFragmentResultListener(
            dialogClass.resultKey,
            activity as LifecycleOwner
        )
        {_, _ -> dialogResultListener()}
    }

    override fun setOnClickListener(l: OnClickListener?) {
        super.setOnClickListener{
            dialogClass
                .show(fragmentActivity.supportFragmentManager)
        }
    }

    protected abstract fun dialogResultListener()
}

class DiscardAllButton(context: Context, attr: AttributeSet):
    CropEntiretyRegardingButton(context, attr, DiscardAllConfirmationDialog()){

    override fun dialogResultListener(){
        typedActivity.invokeSubsequentFragment()
    }
}

class SaveAllButton(context: Context, attr: AttributeSet):
    CropEntiretyRegardingButton(context, attr, SaveAllConfirmationDialog()){

    override fun dialogResultListener() =
        typedActivity.replaceCurrentFragmentWith(SaveAllFragment(), true)
}