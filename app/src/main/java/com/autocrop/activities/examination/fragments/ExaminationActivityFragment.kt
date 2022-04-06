package com.autocrop.activities.examination.fragments

import androidx.viewbinding.ViewBinding
import com.autocrop.activities.examination.ExaminationActivity
import com.autocrop.activities.examination.ExaminationActivityViewModel
import com.autocrop.uicontroller.fragment.ViewBindingHandlingFragment

abstract class ExaminationActivityFragment<VB: ViewBinding>
    : ViewBindingHandlingFragment<ExaminationActivity, ExaminationActivityViewModel, VB>(
        ExaminationActivityViewModel::class.java
    )