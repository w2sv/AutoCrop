package com.autocrop.activities.iodetermination.fragments.apptitle

import android.os.Bundle
import com.autocrop.activities.iodetermination.fragments.IODeterminationActivityFragment
import com.autocrop.utils.android.extensions.animate
import com.autocrop.utils.android.extensions.getLong
import com.daimajia.androidanimations.library.Techniques
import com.w2sv.autocrop.R
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
            onEnd = ::returnToMainActivity,
            delay = resources.getLong(R.integer.delay_small)
        )
    }

    private fun returnToMainActivity() {
        sharedViewModel.singularCropSavingJob?.run {
            invokeOnCompletion {
                typedActivity.startMainActivity()
            }
        }
            ?: typedActivity.startMainActivity()
    }
}