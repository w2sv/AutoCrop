package com.autocrop.activities.iodetermination.fragments

import androidx.viewbinding.ViewBinding
import com.autocrop.activities.iodetermination.IODeterminationActivity
import com.autocrop.activities.iodetermination.IODeterminationActivityViewModel
import com.autocrop.controller.fragment.ApplicationFragment

abstract class IODeterminationActivityFragment<VB : ViewBinding>(bindingClass: Class<VB>) : ApplicationFragment<IODeterminationActivity, VB, IODeterminationActivityViewModel>(
    IODeterminationActivityViewModel::class,
    bindingClass
)