package com.autocrop.uicontroller.fragment

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner

abstract class SharedViewModelHoldingFragment<A: Activity, VM: ViewModel>(viewModelClass: Class<VM>)
        : ExtendedFragment<A>(){

    protected val sharedViewModel: VM by lazy { ViewModelProvider(typedActivity as ViewModelStoreOwner)[viewModelClass] }
}