package com.w2sv.datastore.di

import com.w2sv.datastore.PermissionRepositoryImpl
import com.w2sv.datastore.PreferencesRepositoryImpl
import com.w2sv.domain.repository.PermissionRepository
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

    @Binds
    fun permissionRepository(impl: PermissionRepositoryImpl): PermissionRepository
}