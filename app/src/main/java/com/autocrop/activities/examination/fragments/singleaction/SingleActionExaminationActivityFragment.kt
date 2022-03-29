package com.autocrop.activities.examination.fragments.singleaction

import android.os.Bundle
import android.view.View
import androidx.viewbinding.ViewBinding
import com.autocrop.activities.examination.fragments.ExaminationActivityFragment
import com.autocrop.utils.android.InflateViewBinding

/**
 * Fragment whose sole responsibility is the execution of one singular action,
 * which is directly being launched upon view creation
 */
abstract class SingleActionExaminationActivityFragment<VB: ViewBinding>(inflateViewBinding: InflateViewBinding<VB>)
        : ExaminationActivityFragment<VB>(inflateViewBinding){

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        runAction()
    }

    protected abstract fun runAction()
}