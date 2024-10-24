package com.w2sv.cropbundle.cropping

import androidx.annotation.IntRange

/**
 * := (255 - [EDGE_CANDIDATE_THRESHOLD_MIN]) / [CROP_SENSITIVITY_MAX]
 */
private const val EDGE_CANDIDATE_THRESHOLD_PER_SENSITIVITY_STEP: Float = 20.5f
private const val EDGE_CANDIDATE_THRESHOLD_MIN: Int = 50
const val CROP_SENSITIVITY_MAX: Int = 10

@Retention(AnnotationRetention.BINARY)
@IntRange(from = 0, to = CROP_SENSITIVITY_MAX.toLong())
annotation class CropSensitivity

@IntRange(50, 255)
internal fun edgeCandidateThreshold(@CropSensitivity cropSensitivity: Int): Int =
    ((CROP_SENSITIVITY_MAX - cropSensitivity) * EDGE_CANDIDATE_THRESHOLD_PER_SENSITIVITY_STEP).toInt() + EDGE_CANDIDATE_THRESHOLD_MIN

//@IntRange(0, CROP_SENSITIVITY_MAX)
//fun cropSensitivity(edgeCandidateThreshold: Int): Int =
//    -((edgeCandidateThreshold - EDGE_CANDIDATE_THRESHOLD_MIN) / EDGE_CANDIDATE_THRESHOLD_PORTION_PER_SENSITIVITY_STEP - CROP_SENSITIVITY_MAX).toInt()