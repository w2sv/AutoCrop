package com.autocrop.activities.main.fragments

import androidx.viewbinding.ViewBinding
import com.autocrop.activities.main.MainActivity
import com.autocrop.activities.main.MainActivityViewModel
import com.autocrop.uicontroller.fragment.SharedViewModelAndViewBindingHoldingFragment
import com.autocrop.uicontroller.fragment.ViewBindingHoldingFragment

abstract class MainActivityFragment<VB: ViewBinding>:
    SharedViewModelAndViewBindingHoldingFragment<MainActivity, VB, MainActivityViewModel>(MainActivityViewModel::class.java)