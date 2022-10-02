package com.autocrop.activities.iodetermination.fragments.apptitle

import android.os.Bundle
import com.autocrop.activities.iodetermination.fragments.IODeterminationActivityFragment
import com.autocrop.utils.android.extensions.animate
import com.daimajia.androidanimations.library.Techniques
import com.w2sv.autocrop.databinding.FragmentApptitleBinding

class AppTitleFragment
    : IODeterminationActivityFragment<FragmentApptitleBinding>(FragmentApptitleBinding::class.java) {

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