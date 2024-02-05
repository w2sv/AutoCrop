package com.w2sv.datastore

import com.w2sv.domain.repository.PreferencesRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@InstallIn(SingletonComponent::class)
@Module
interface RepositoryBinderModule {
    @Binds
    fun preferencesRepository(impl: PreferencesRepositoryImpl): PreferencesRepository
}