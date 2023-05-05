package com.w2sv.common.datastore

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

object PreferencesKey {
    val ONBOARDING_DONE = booleanPreferencesKey("onboardingDone")
    val COMPARISON_INSTRUCTIONS_SHOWN = booleanPreferencesKey("comparisonInstructionsShown")
    val AUTO_SCROLL = booleanPreferencesKey("autoScroll")
    val DELETE_SCREENSHOTS = booleanPreferencesKey("deleteScreenshots")

    val EDGE_CANDIDATE_THRESHOLD = intPreferencesKey("edgeCandidateThreshold")
    val CROP_ADJUSTMENT_MODE = intPreferencesKey("cropAdjustmentMode")

    val TREE_URI = stringPreferencesKey("treeUri")
    val DOCUMENT_URI = stringPreferencesKey("documentUri")
}