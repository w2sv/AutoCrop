package com.autocrop.activities.cropping.fragments

import androidx.viewbinding.ViewBinding
import com.autocrop.activities.cropping.CroppingActivity
import com.autocrop.activities.cropping.CroppingActivityViewModel
import com.autocrop.ui.controller.fragment.ApplicationFragment

abstract class CroppingActivityFragment<VB: ViewBinding>(bindingClass: Class<VB>)
    : ApplicationFragment<CroppingActivity, VB, CroppingActivityViewModel>(
        CroppingActivityViewModel::class,
        bindingClass
    )