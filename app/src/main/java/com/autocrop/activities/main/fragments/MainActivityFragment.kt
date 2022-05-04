package com.autocrop.activities.main.fragments

import androidx.viewbinding.ViewBinding
import com.autocrop.activities.main.MainActivity
import com.autocrop.activities.main.MainActivityViewModel
import com.autocrop.uicontroller.fragment.ApplicationFragment

abstract class MainActivityFragment<VB: ViewBinding>(bindingClass: Class<VB>):
    ApplicationFragment<MainActivity, VB, MainActivityViewModel>(
        MainActivityViewModel::class.java,
        bindingClass
    )