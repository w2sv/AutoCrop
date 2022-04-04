package com.autocrop.activities.cropping.fragments

import androidx.viewbinding.ViewBinding
import com.autocrop.activities.cropping.CroppingActivity
import com.autocrop.activities.cropping.CroppingActivityViewModel
import com.autocrop.uicontroller.fragment.ViewBindingHandlingFragment

abstract class CroppingActivityFragment<VB: ViewBinding>
    : ViewBindingHandlingFragment<CroppingActivity, CroppingActivityViewModel, VB>(
        CroppingActivityViewModel::class.java
    )