package com.autocrop.activities.examination.fragments

import androidx.viewbinding.ViewBinding
import com.autocrop.activities.examination.ExaminationActivity
import com.autocrop.activities.examination.ExaminationActivityViewModel
import com.autocrop.uicontroller.fragment.ApplicationFragment

abstract class ExaminationActivityFragment<VB: ViewBinding>
    : ApplicationFragment<ExaminationActivity, VB, ExaminationActivityViewModel>(
        ExaminationActivityViewModel::class.java
    )