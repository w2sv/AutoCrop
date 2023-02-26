package com.w2sv.autocrop.ui.model

/**
 * := (255 - [EDGE_CANDIDATE_THRESHOLD_MIN]) / [CROP_SENSITIVITY_MAX]
 */
private const val EDGE_CANDIDATE_THRESHOLD_PORTION_PER_SENSITIVITY_STEP: Float = 10.25f
private const val EDGE_CANDIDATE_THRESHOLD_MIN: Int = 50
const val CROP_SENSITIVITY_MAX: Int = 20

fun edgeCandidateThreshold(sensitivityLevel: Int): Int =
    ((CROP_SENSITIVITY_MAX - sensitivityLevel) * EDGE_CANDIDATE_THRESHOLD_PORTION_PER_SENSITIVITY_STEP).toInt() + EDGE_CANDIDATE_THRESHOLD_MIN

fun cropSensitivity(edgeCandidateThreshold: Int): Int =
    -((edgeCandidateThreshold - EDGE_CANDIDATE_THRESHOLD_MIN) / EDGE_CANDIDATE_THRESHOLD_PORTION_PER_SENSITIVITY_STEP - CROP_SENSITIVITY_MAX).toInt()