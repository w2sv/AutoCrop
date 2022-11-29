package com.w2sv.autocrop.activities.cropexamination.fragments.apptitle

import androidx.fragment.app.activityViewModels
import com.daimajia.androidanimations.library.Techniques
import com.w2sv.androidutils.extensions.invokeOnCompletion
import com.w2sv.autocrop.activities.ApplicationFragment
import com.w2sv.autocrop.activities.cropexamination.CropExaminationActivity
import com.w2sv.autocrop.databinding.FragmentApptitleBinding
import com.w2sv.autocrop.ui.animationComposer

class AppTitleFragment
    : ApplicationFragment<FragmentApptitleBinding>(FragmentApptitleBinding::class.java) {

    private val activityViewModel by activityViewModels<CropExaminationActivity.ViewModel>()

    override fun onResume() {
        super.onResume()

        with(binding.appTitleTextView) {
            animationComposer(
                listOf(
                    Techniques.Shake,
                    Techniques.Wobble,
                    Techniques.Wave,
                    Techniques.Tada
                )
                    .random()
            )
                .onEnd {
                    activityViewModel.cropBundleProcessingJob.invokeOnCompletion {
                        castActivity<CropExaminationActivity>().startMainActivity()
                    }
                }
                .playOn(this)
        }
    }
}