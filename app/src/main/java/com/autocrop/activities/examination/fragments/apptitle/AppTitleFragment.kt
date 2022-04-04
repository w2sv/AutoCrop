package com.autocrop.activities.examination.fragments.apptitle

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import com.autocrop.activities.examination.fragments.ExaminationActivityFragment
import com.w2sv.autocrop.databinding.ActivityExaminationFragmentApptitleBinding


class AppTitleFragment
    : ExaminationActivityFragment<ActivityExaminationFragmentApptitleBinding>() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Handler(Looper.getMainLooper()).postDelayed(
            typedActivity::returnToMainActivity,
            1000
        )
    }
}