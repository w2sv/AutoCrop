package com.w2sv.autocrop.activities.cropping.fragments

import androidx.viewbinding.ViewBinding
import com.w2sv.autocrop.activities.cropping.CropActivity
import com.w2sv.autocrop.activities.cropping.CropActivityViewModel
import com.w2sv.autocrop.controller.fragment.ApplicationFragment

abstract class CropActivityFragment<VB : ViewBinding>(bindingClass: Class<VB>) : ApplicationFragment<CropActivity, VB, CropActivityViewModel>(
    CropActivityViewModel::class,
    bindingClass
)