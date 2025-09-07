package com.w2sv.autocrop.di

import com.w2sv.domain.session.CropSession
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
    fun provideCropSession(): CropSession =
        CropSession()
}
