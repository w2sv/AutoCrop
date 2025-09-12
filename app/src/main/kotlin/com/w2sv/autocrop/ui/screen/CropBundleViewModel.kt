package com.w2sv.autocrop.ui.screen

import androidx.annotation.MainThread
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.lifecycle.ViewModel
import com.w2sv.autocrop.R
import com.w2sv.cropping.session.CropSession
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.lifecycle.withCreationCallback
import javax.inject.Inject

inline fun <reified VM : ViewModel> Fragment.cropNavGraphViewModel(): Lazy<VM> =
    hiltNavGraphViewModels<VM>(R.id.crop_nav_graph)

@MainThread
inline fun <reified VM : ViewModel, F : CropSessionAccessingViewModelFactory<VM>> Fragment.cropSessionInjectedViewModel(): Lazy<VM> {
    val cropSessionViewModel by cropNavGraphViewModel<CropSessionViewModel>()
    return viewModels<VM>(
        extrasProducer = {
            defaultViewModelCreationExtras
                .withCreationCallback<F> { factory ->
                    factory.create(cropSessionViewModel.cropSession)
                }
        }
    )
}

@HiltViewModel
class CropSessionViewModel @Inject constructor(val cropSession: CropSession) : ViewModel()

interface CropSessionAccessingViewModelFactory<VM : ViewModel> {
    fun create(cropSession: CropSession): VM
}
