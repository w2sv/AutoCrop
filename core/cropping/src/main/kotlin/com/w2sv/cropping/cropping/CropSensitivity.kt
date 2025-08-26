package com.w2sv.cropping.cropping

import androidx.annotation.IntRange
import com.w2sv.domain.model.CROP_SENSITIVITY_MAX
import com.w2sv.domain.model.CropSensitivity

/**
 * := (255 - [EDGE_CANDIDATE_THRESHOLD_MIN]) / [CROP_SENSITIVITY_MAX]
 */
private const val EDGE_CANDIDATE_THRESHOLD_PER_SENSITIVITY_STEP: Float = 20.5f
private const val EDGE_CANDIDATE_THRESHOLD_MIN: Int = 50

@IntRange(50, 255)
internal fun edgeCandidateThreshold(@CropSensitivity cropSensitivity: Int): Int =
    ((CROP_SENSITIVITY_MAX - cropSensitivity) * EDGE_CANDIDATE_THRESHOLD_PER_SENSITIVITY_STEP).toInt() + EDGE_CANDIDATE_THRESHOLD_MIN

// @IntRange(0, CROP_SENSITIVITY_MAX)
// fun cropSensitivity(edgeCandidateThreshold: Int): Int =
//    -((edgeCandidateThreshold - EDGE_CANDIDATE_THRESHOLD_MIN) / EDGE_CANDIDATE_THRESHOLD_PORTION_PER_SENSITIVITY_STEP - CROP_SENSITIVITY_MAX).toInt()
