package com.w2sv.cropbundle.cropping

import android.content.Context
import android.graphics.Bitmap
import com.w2sv.common.datastore.Repository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import javax.inject.Inject

class Cropper @Inject constructor(private val repository: Repository) {

    @InstallIn(SingletonComponent::class)
    @EntryPoint
    interface CropperEntryPoint {
        fun getCropper(): Cropper
    }

    companion object {
        fun getInstance(context: Context): Cropper =
            EntryPointAccessors.fromApplication(
                context,
                CropperEntryPoint::class.java
            )
                .getCropper()

        fun invoke(src: Bitmap, context: Context): CropResult? =
            getInstance(context).invoke(src)
    }

    private fun invoke(src: Bitmap): CropResult? =
        src.crop(repository.edgeCandidateThreshold.value.toDouble())
}