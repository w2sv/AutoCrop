package com.autocrop.activities.examination.fragments.apptitle

import android.os.Bundle
import android.view.View
import com.autocrop.activities.examination.fragments.ExaminationActivityFragment
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo
import com.w2sv.autocrop.R
import com.w2sv.autocrop.databinding.ActivityExaminationFragmentApptitleBinding

class AppTitleFragment
    : ExaminationActivityFragment<ActivityExaminationFragmentApptitleBinding>() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        YoYo.with(listOf(Techniques.Shake, Techniques.Wobble, Techniques.Wave, Techniques.Tada).random())
            .duration(requireContext().resources.getInteger(R.integer.yoyo_animation_duration).toLong())
            .onEnd{typedActivity.returnToMainActivity()}
            .playOn(binding.appTitleTextView)
    }
}