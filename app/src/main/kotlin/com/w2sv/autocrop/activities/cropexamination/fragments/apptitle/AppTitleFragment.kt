package com.w2sv.autocrop.activities.cropexamination.fragments.apptitle

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import com.daimajia.androidanimations.library.Techniques
import com.w2sv.androidutils.extensions.getLong
import com.w2sv.autocrop.R
import com.w2sv.autocrop.activities.cropexamination.CropExaminationActivity
import com.w2sv.autocrop.activities.cropexamination.CropExaminationActivityViewModel
import com.w2sv.autocrop.controller.ApplicationFragment
import com.w2sv.autocrop.databinding.FragmentApptitleBinding
import com.w2sv.autocrop.ui.animationComposer

class AppTitleFragment
    : ApplicationFragment<FragmentApptitleBinding>(FragmentApptitleBinding::class.java) {

    private val activityViewModel by activityViewModels<CropExaminationActivityViewModel>()

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
                    activityViewModel.singularCropSavingJob?.run {
                        invokeOnCompletion {
                            castActivity<CropExaminationActivity>().startMainActivity()
                        }
                    }
                        ?: castActivity<CropExaminationActivity>().startMainActivity()
                }
                .playOn(this)
        }
    }
}