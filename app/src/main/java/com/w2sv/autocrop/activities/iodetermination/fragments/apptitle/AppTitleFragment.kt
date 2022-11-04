package com.w2sv.autocrop.activities.iodetermination.fragments.apptitle

import android.os.Bundle
import android.view.View
import com.daimajia.androidanimations.library.Techniques
import com.w2sv.autocrop.R
import com.w2sv.autocrop.activities.iodetermination.fragments.IODeterminationActivityFragment
import com.w2sv.autocrop.databinding.FragmentApptitleBinding
import com.w2sv.autocrop.utils.android.extensions.animationComposer
import com.w2sv.autocrop.utils.android.extensions.getLong

class AppTitleFragment
    : IODeterminationActivityFragment<FragmentApptitleBinding>(FragmentApptitleBinding::class.java) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding.appTitleTextView) {
            animationComposer(
                listOf(
                    Techniques.Shake,
                    Techniques.Wobble,
                    Techniques.Wave,
                    Techniques.Tada
                )
                    .random(),
                delay = resources.getLong(R.integer.delay_small)
            )
                .onEnd {
                    applicationViewModel.singularCropSavingJob?.run {
                        invokeOnCompletion {
                            castActivity.startMainActivity()
                        }
                    }
                        ?: castActivity.startMainActivity()
                }
                .playOn(this)
        }
    }
}