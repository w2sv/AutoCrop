package com.autocrop.retriever.viewmodel

import androidx.lifecycle.ViewModel

interface ViewModelRetriever<VM: ViewModel>{
    val sharedViewModel: VM
}