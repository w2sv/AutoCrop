package com.autocrop.activities.examination.fragments.viewpager.views

import android.content.Context
import android.util.AttributeSet
import android.view.MenuItem
import androidx.lifecycle.LifecycleOwner
import com.autocrop.activities.examination.ExaminationActivity
import com.autocrop.activities.examination.fragments.saveall.SaveAllFragment
import com.autocrop.activities.examination.fragments.viewpager.dialogs.CropEntiretyProcedureDialog
import com.autocrop.activities.examination.fragments.viewpager.dialogs.DiscardAllConfirmationDialog
import com.autocrop.activities.examination.fragments.viewpager.dialogs.SaveAllConfirmationDialog
import com.autocrop.retriever.activity.ActivityRetriever
import com.autocrop.retriever.activity.ContextBasedActivityRetriever

abstract class AllCropsMenuItemWrapper(context: Context,
                                       private val dialogClass: CropEntiretyProcedureDialog)
    : ActivityRetriever<ExaminationActivity> by ContextBasedActivityRetriever(context) {

    init {
        fragmentActivity.supportFragmentManager.setFragmentResultListener(
            dialogClass.resultKey,
            activity as LifecycleOwner
        )
        {_, _ -> dialogResultListener()}
    }

    protected abstract fun dialogResultListener()

    fun onClickListener() =
        dialogClass
            .show(fragmentActivity.supportFragmentManager)
}

class DiscardAllButton(context: Context):
    AllCropsMenuItemWrapper(context, DiscardAllConfirmationDialog()){

    override fun dialogResultListener(){
        typedActivity.invokeSubsequentFragment()
    }
}

class SaveAllButton(context: Context):
    AllCropsMenuItemWrapper(context, SaveAllConfirmationDialog()){

    override fun dialogResultListener() =
        typedActivity.replaceCurrentFragmentWith(SaveAllFragment(), true)
}