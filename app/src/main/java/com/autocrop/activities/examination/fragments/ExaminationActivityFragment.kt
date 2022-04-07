package com.autocrop.activities.examination.fragments

import androidx.viewbinding.ViewBinding
import com.autocrop.activities.examination.ExaminationActivity
import com.autocrop.activities.examination.ExaminationActivityViewModel
import com.autocrop.uicontroller.fragment.SharedViewModelAndViewBindingHoldingFragment

abstract class ExaminationActivityFragment<VB: ViewBinding>
    : SharedViewModelAndViewBindingHoldingFragment<ExaminationActivity, VB, ExaminationActivityViewModel>(
        ExaminationActivityViewModel::class.java
    )