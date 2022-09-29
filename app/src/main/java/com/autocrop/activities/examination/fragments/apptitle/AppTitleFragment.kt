package com.autocrop.activities.examination.fragments.apptitle

import android.os.Bundle
import com.autocrop.activities.examination.fragments.ExaminationActivityFragment
import com.autocrop.ui.elements.view.animate
import com.daimajia.androidanimations.library.Techniques
import com.w2sv.autocrop.databinding.FragmentApptitleBinding

class AppTitleFragment
    : ExaminationActivityFragment<FragmentApptitleBinding>(FragmentApptitleBinding::class.java) {

    override fun onViewCreatedCore(savedInstanceState: Bundle?) {
        binding.appTitleTextView.animate(
            listOf(
                Techniques.Shake,
                Techniques.Wobble,
                Techniques.Wave,
                Techniques.Tada
            )
                .random(),
            onEnd = ::waitTilCroppingJobFinishedOrReturnToMainActivityDirectly
        )
    }

    private fun waitTilCroppingJobFinishedOrReturnToMainActivityDirectly(){
        sharedViewModel.singularCropSavingJob?.run{
            invokeOnCompletion {
                typedActivity.returnToMainActivity()
            }
        } ?: typedActivity.returnToMainActivity()
    }
}