package com.w2sv.preferences

import android.content.SharedPreferences
import com.w2sv.common.DEFAULT_CROP_EDGE_CANDIDATE_THRESHOLD
import com.w2sv.typedpreferences.IntPreferences
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IntPreferences @Inject constructor(appPreferences: SharedPreferences) : IntPreferences(
    "cropEdgeCandidateThreshold" to DEFAULT_CROP_EDGE_CANDIDATE_THRESHOLD.toInt(),
    sharedPreferences = appPreferences
) {
    var cropEdgeCandidateThreshold by this
}