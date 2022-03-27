package com.autocrop.activities.examination.fragments

import androidx.viewbinding.ViewBinding
import com.autocrop.activities.examination.ExaminationActivity
import com.autocrop.activities.examination.ExaminationViewModel
import com.autocrop.utils.android.BindingHandlingFragment
import com.autocrop.utils.android.InflateViewBinding

abstract class ExaminationActivityFragment<VB: ViewBinding>(inflateViewBinding: InflateViewBinding<VB>)
    : BindingHandlingFragment<ExaminationActivity, ExaminationViewModel, VB>(ExaminationViewModel::class.java, inflateViewBinding)