package com.w2sv.autocrop.activities.main.fragments

import androidx.viewbinding.ViewBinding
import com.w2sv.autocrop.activities.main.MainActivity
import com.w2sv.autocrop.activities.main.MainActivityViewModel
import com.w2sv.autocrop.controller.fragment.ApplicationFragment

abstract class MainActivityFragment<VB : ViewBinding>(bindingClass: Class<VB>) :
    ApplicationFragment<MainActivity, VB, MainActivityViewModel>(
        MainActivityViewModel::class.java,
        bindingClass
    )