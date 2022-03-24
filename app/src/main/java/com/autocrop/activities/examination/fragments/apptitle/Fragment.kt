package com.autocrop.activities.examination.fragments.apptitle

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.fragment.app.Fragment
import com.autocrop.activities.examination.ExaminationActivity
import com.autocrop.activities.examination.fragments.ExaminationActivityFragment
import com.w2sv.autocrop.R


class AppTitleFragment : Fragment(R.layout.activity_examination_fragment_apptitle) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        delayedReturnToMainActivity()
    }

    private fun delayedReturnToMainActivity() {
        Handler(Looper.getMainLooper()).postDelayed(
            (activity as ExaminationActivity)::returnToMainActivity,
            1000
        )
    }
}