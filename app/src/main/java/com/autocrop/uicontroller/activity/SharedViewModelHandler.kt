package com.autocrop.uicontroller.activity

import androidx.lifecycle.ViewModel

interface SharedViewModelHandler<T: ViewModel> {
    var sharedViewModel: T

    fun setSharedViewModel(){
        sharedViewModel = provideSharedViewModel()
    }
    fun provideSharedViewModel(): T
}