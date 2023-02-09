package com.w2sv.autocrop.activities.examination.fragments.apptitle

import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.daimajia.androidanimations.library.Techniques
import com.w2sv.androidutils.extensions.invokeOnCompletion
import com.w2sv.autocrop.activities.AppFragment
import com.w2sv.autocrop.activities.examination.ExaminationActivity
import com.w2sv.autocrop.databinding.FragmentApptitleBinding
import com.w2sv.autocrop.ui.views.animationComposer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AppTitleFragment
    : AppFragment<FragmentApptitleBinding>(FragmentApptitleBinding::class.java) {

    override fun onResume() {
        super.onResume()

        lifecycleScope.launch(Dispatchers.Main) {
            binding.appTitleTextView.animationComposer(
                listOf(
                    Techniques.Wobble,
                    Techniques.Wave,
                    Techniques.Tada
                )
                    .random()
            )
                .onEnd {
                    activityViewModels<ExaminationActivity.ViewModel>().value.cropProcessingCoroutine.invokeOnCompletion {
                        castActivity<ExaminationActivity>().invokeSubsequentController(this@AppTitleFragment)
                    }
                }
                .play()
        }
    }
}