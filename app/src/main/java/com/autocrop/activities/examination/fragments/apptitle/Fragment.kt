package com.autocrop.activities.examination.fragments.apptitle

import android.os.Bundle
import android.os.Handler
import android.view.View
import com.autocrop.activities.examination.fragments.ExaminationActivityFragment
import com.bunsenbrenner.screenshotboundremoval.R


class AppTitleFragment : ExaminationActivityFragment(R.layout.activity_examination_apptitle) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        conductDelayedReturnToMainActivity()
    }

    private fun conductDelayedReturnToMainActivity() {
        Handler().postDelayed(
            { activity.returnToMainActivity() },
            1000
        )
    }
}