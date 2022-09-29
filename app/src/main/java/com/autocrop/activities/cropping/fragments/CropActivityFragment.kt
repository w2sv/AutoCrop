package com.autocrop.activities.cropping.fragments

import androidx.viewbinding.ViewBinding
import com.autocrop.activities.cropping.CropActivity
import com.autocrop.activities.cropping.CropActivityViewModel
import com.autocrop.ui.controller.fragment.ApplicationFragment

abstract class CropActivityFragment<VB: ViewBinding>(bindingClass: Class<VB>)
    : ApplicationFragment<CropActivity, VB, CropActivityViewModel>(
        CropActivityViewModel::class,
        bindingClass
    )