package com.autocrop.activities.examination.fragments.singleaction.apptitle

import android.os.Handler
import android.os.Looper
import com.autocrop.activities.examination.fragments.singleaction.SingleActionExaminationActivityFragment
import com.w2sv.autocrop.R


class AppTitleFragment : SingleActionExaminationActivityFragment(R.layout.activity_examination_fragment_apptitle) {
    override fun runAction() {
        Handler(Looper.getMainLooper()).postDelayed(
            activity::returnToMainActivity,
            1000
        )
    }
}