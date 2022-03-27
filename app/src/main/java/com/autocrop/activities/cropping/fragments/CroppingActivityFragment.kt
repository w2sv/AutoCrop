package com.autocrop.activities.cropping.fragments

import androidx.viewbinding.ViewBinding
import com.autocrop.activities.cropping.CroppingActivity
import com.autocrop.activities.cropping.CroppingActivityViewModel
import com.autocrop.utils.android.BindingHandlingFragment
import com.autocrop.utils.android.InflateViewBinding

abstract class CroppingActivityFragment<VB: ViewBinding>(inflateViewBinding: InflateViewBinding<VB>)
    : BindingHandlingFragment<CroppingActivity, CroppingActivityViewModel, VB>(CroppingActivityViewModel::class.java, inflateViewBinding)