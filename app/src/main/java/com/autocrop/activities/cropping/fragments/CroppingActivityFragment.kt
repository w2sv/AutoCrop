package com.autocrop.activities.cropping.fragments

import androidx.viewbinding.ViewBinding
import com.autocrop.activities.cropping.CroppingActivity
import com.autocrop.activities.cropping.CroppingActivityViewModel
import com.autocrop.uicontroller.fragment.ApplicationFragment

abstract class CroppingActivityFragment<VB: ViewBinding>
    : ApplicationFragment<CroppingActivity, VB, CroppingActivityViewModel>(
        CroppingActivityViewModel::class.java
    )