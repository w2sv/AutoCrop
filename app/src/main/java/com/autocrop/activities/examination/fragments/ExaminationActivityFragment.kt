package com.autocrop.activities.examination.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.autocrop.activities.examination.ExaminationActivity
import com.autocrop.activities.examination.ExaminationViewModel


abstract class ExaminationActivityFragment(layoutId: Int): Fragment(layoutId){
    protected val viewModel: ExaminationViewModel by activityViewModels()

    /**
     * Retyped [androidx.fragment.app.Fragment.requireActivity]
     */
    val activity: ExaminationActivity
        get() = super.requireActivity() as ExaminationActivity
}

//abstract class BindingHandlingExaminationActivityFragment<T: ViewBinding>(layoutId: Int): ExaminationActivityFragment(layoutId){
//    protected var _binding: T? = null
//    protected val binding: T
//        get() = _binding!!
//
//    override fun onDestroyView() {
//        super.onDestroyView()
//        _binding = null
//    }
//}