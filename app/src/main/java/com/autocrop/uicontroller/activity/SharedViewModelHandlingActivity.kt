package com.autocrop.uicontroller.activity

import androidx.lifecycle.ViewModel

interface SharedViewModelHandlingActivity<T: ViewModel> {
    var sharedViewModel: T

    fun setSharedViewModel(){
        sharedViewModel = provideSharedViewModel()
    }

    /**
     * Instantiate/retrieve instance of [T]
     */
    fun provideSharedViewModel(): T
}