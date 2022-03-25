package com.autocrop.activities.examination.fragments.singleaction

import android.os.Bundle
import android.view.View
import com.autocrop.activities.examination.fragments.ExaminationActivityFragment

/**
 * Fragment whose responsibility is the execution of one singular action,
 * which is directly being launched upon view creation
 */
abstract class SingleActionExaminationActivityFragment(layoutId: Int): ExaminationActivityFragment(layoutId){
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        runAction()
    }

    protected abstract fun runAction()
}