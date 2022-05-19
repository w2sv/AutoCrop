package com.autocrop.uicontroller

import androidx.lifecycle.ViewModel

interface ViewModelRetriever<VM: ViewModel>{
    val sharedViewModel: VM
}