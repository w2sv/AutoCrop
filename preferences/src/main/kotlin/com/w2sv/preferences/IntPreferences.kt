package com.w2sv.preferences

import android.content.SharedPreferences
import com.w2sv.common.DEFAULT_EDGE_CANDIDATE_THRESHOLD
import com.w2sv.androidutils.typedpreferences.IntPreferences
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IntPreferences @Inject constructor(appPreferences: SharedPreferences) : IntPreferences(
    "edgeCandidateThreshold" to DEFAULT_EDGE_CANDIDATE_THRESHOLD.toInt(),
    sharedPreferences = appPreferences
) {
    var edgeCandidateThreshold by this

    val cropEdgeCandidateThresholdDouble: Double get() = edgeCandidateThreshold.toDouble()
}