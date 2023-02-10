package com.w2sv.autocrop.di

import android.content.ContentResolver
import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@InstallIn(SingletonComponent::class)
@Module
object ContentResolverModule {

    @Provides
    fun provide(@ApplicationContext context: Context): ContentResolver =
        context.contentResolver
}