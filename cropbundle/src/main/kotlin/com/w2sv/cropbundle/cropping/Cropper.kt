package com.w2sv.cropbundle.cropping

import android.content.Context
import android.graphics.Bitmap
import com.w2sv.preferences.IntPreferences
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import javax.inject.Inject

class Cropper @Inject constructor(private val intPreferences: IntPreferences) {

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

        fun getCropEdges(src: Bitmap, context: Context): CropEdges? =
            getInstance(context).getCropEdges(src)
    }

    fun getCropEdges(src: Bitmap): CropEdges? =
        src.getCropEdges(intPreferences.cropEdgeCandidateThreshold.toDouble())
}