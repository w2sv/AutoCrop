package com.autocrop.activities.examination.fragments

import android.view.View
import androidx.fragment.app.Fragment
import com.autocrop.activities.examination.ExaminationActivity


abstract class ExaminationActivityFragment(layoutId: Int): Fragment(layoutId){
    /**
     * Retyped [androidx.fragment.app.Fragment.requireActivity]
     */
    val examinationActivity: ExaminationActivity
        get() = super.requireActivity() as ExaminationActivity

    /**
     * Convenience wrapper of [androidx.fragment.app.Fragment.requireView]
     */
    fun <T: View> findViewById(id: Int): T = requireView().findViewById(id)
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