package com.autocrop.activities.examination.fragments.apptitle

import android.os.Bundle
import android.os.Handler
import android.view.View
import com.autocrop.activities.examination.fragments.DownstreamExaminationActivityFragment
import com.w2sv.autocrop.R


class AppTitleFragment : DownstreamExaminationActivityFragment(R.layout.activity_examination_apptitle) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        delayedReturnToMainActivity()
    }

    private fun delayedReturnToMainActivity() {
        Handler().postDelayed(
            examinationActivity::returnToMainActivity,
            1000
        )
    }
}