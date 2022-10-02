package com.autocrop.retriever.viewmodel

import androidx.lifecycle.ViewModel

interface SharedViewModelRetriever<VM: ViewModel>{
    val sharedViewModel: VM
}