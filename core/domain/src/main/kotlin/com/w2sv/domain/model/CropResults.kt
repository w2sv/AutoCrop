package com.w2sv.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class CropResults(val unopenableImageCount: Int, val uncroppableImageCount: Int) : Parcelable
