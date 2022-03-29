package com.autocrop.activities.examination.fragments.singleaction.apptitle

import android.os.Handler
import android.os.Looper
import com.autocrop.activities.examination.fragments.singleaction.SingleActionExaminationActivityFragment
import com.w2sv.autocrop.R
import com.w2sv.autocrop.databinding.ActivityExaminationFragmentApptitleBinding


class AppTitleFragment
    : SingleActionExaminationActivityFragment<ActivityExaminationFragmentApptitleBinding>(ActivityExaminationFragmentApptitleBinding::inflate) {

    override fun runAction() {
        Handler(Looper.getMainLooper()).postDelayed(
            activity::returnToMainActivity,
            1000
        )
    }
}