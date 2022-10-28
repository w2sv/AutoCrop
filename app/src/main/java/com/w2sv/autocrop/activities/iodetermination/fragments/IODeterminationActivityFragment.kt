package com.w2sv.autocrop.activities.iodetermination.fragments

import androidx.viewbinding.ViewBinding
import com.w2sv.autocrop.activities.iodetermination.IODeterminationActivity
import com.w2sv.autocrop.activities.iodetermination.IODeterminationActivityViewModel
import com.w2sv.autocrop.controller.fragment.ApplicationFragment

abstract class IODeterminationActivityFragment<VB : ViewBinding>(bindingClass: Class<VB>) : ApplicationFragment<IODeterminationActivity, VB, IODeterminationActivityViewModel>(
    IODeterminationActivityViewModel::class,
    bindingClass
)