package com.autocrop.activities.examination.fragments

import android.view.View
import androidx.fragment.app.Fragment
import com.autocrop.activities.examination.ExaminationActivity


abstract class ExaminationActivityFragment: Fragment(){
    val activity: ExaminationActivity
        get() = super.getActivity()!! as ExaminationActivity

    fun <T: View> findViewById(id: Int): T = view!!.findViewById(id)
}