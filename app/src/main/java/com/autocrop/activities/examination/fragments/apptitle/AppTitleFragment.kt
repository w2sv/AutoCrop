package com.autocrop.activities.examination.fragments.apptitle

import android.os.Bundle
import com.autocrop.activities.examination.fragments.ExaminationActivityFragment
import com.autocrop.ui.elements.view.animate
import com.daimajia.androidanimations.library.Techniques
import com.w2sv.autocrop.databinding.ExaminationFragmentApptitleBinding

class AppTitleFragment
    : ExaminationActivityFragment<ExaminationFragmentApptitleBinding>(ExaminationFragmentApptitleBinding::class.java) {

    override fun onViewCreatedCore(savedInstanceState: Bundle?) {
        binding.appTitleTextView.animate(
            listOf(
                Techniques.Shake,
                Techniques.Wobble,
                Techniques.Wave,
                Techniques.Tada
            )
                .random(),
            onEnd = typedActivity::returnToMainActivity
        )
    }
}