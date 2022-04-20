package com.autocrop.activities.main.fragments

import androidx.viewbinding.ViewBinding
import com.autocrop.activities.main.MainActivity
import com.autocrop.uicontroller.fragment.ViewBindingHoldingFragment

abstract class MainActivityFragment<VB: ViewBinding>: ViewBindingHoldingFragment<MainActivity, VB>()