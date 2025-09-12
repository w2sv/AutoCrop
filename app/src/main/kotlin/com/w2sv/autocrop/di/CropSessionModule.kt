package com.w2sv.autocrop.di

import com.w2sv.cropping.io.CropBundleIOProcessingUseCase
import com.w2sv.cropping.session.CropSession
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped

@Module
@InstallIn(ViewModelComponent::class)
object CropSessionModule {

    @Provides
    @ViewModelScoped
    fun provideCropSession(cropBundleIOProcessingUseCase: CropBundleIOProcessingUseCase): CropSession =
        CropSession(cropBundleIOProcessingUseCase)
}
