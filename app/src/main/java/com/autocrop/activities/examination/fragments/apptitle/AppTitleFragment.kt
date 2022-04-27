package com.autocrop.activities.examination.fragments.apptitle

import android.os.Bundle
import android.view.View
import com.autocrop.activities.examination.fragments.ExaminationActivityFragment
import com.autocrop.uielements.view.animate
import com.daimajia.androidanimations.library.Techniques
import com.w2sv.autocrop.databinding.ExaminationFragmentApptitleBinding

class AppTitleFragment
    : ExaminationActivityFragment<ExaminationFragmentApptitleBinding>() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.appTitleTextView.animate(
            listOf(Techniques.Shake, Techniques.Wobble, Techniques.Wave, Techniques.Tada).random()
        ){typedActivity.returnToMainActivity()}
    }
}