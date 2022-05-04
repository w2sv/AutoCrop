package com.autocrop.uicontroller

import androidx.lifecycle.ViewModel

interface ViewModelHolder<VM: ViewModel>{
    val sharedViewModel: VM
}