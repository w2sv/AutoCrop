package com.autocrop.activities.examination.fragments

import androidx.viewbinding.ViewBinding
import com.autocrop.activities.examination.ExaminationActivity
import com.autocrop.activities.examination.ExaminationActivityViewModel
import com.autocrop.ui.controller.fragment.ApplicationFragment

abstract class ExaminationActivityFragment<VB: ViewBinding>(bindingClass: Class<VB>)
    : ApplicationFragment<ExaminationActivity, VB, ExaminationActivityViewModel>(
        ExaminationActivityViewModel::class,
        bindingClass
    )