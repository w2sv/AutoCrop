package com.w2sv.domain.model

import androidx.annotation.IntRange

const val CROP_SENSITIVITY_MAX: Int = 10

@Retention(AnnotationRetention.BINARY)
@IntRange(from = 0, to = CROP_SENSITIVITY_MAX.toLong())
annotation class CropSensitivity
