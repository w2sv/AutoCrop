package com.w2sv.autocrop.activities.cropexamination.fragments.apptitle

import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.daimajia.androidanimations.library.Techniques
import com.w2sv.androidutils.extensions.invokeOnCompletion
import com.w2sv.autocrop.activities.ApplicationFragment
import com.w2sv.autocrop.activities.cropexamination.CropExaminationActivity
import com.w2sv.autocrop.databinding.FragmentApptitleBinding
import com.w2sv.autocrop.ui.animationComposer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AppTitleFragment
    : ApplicationFragment<FragmentApptitleBinding>(FragmentApptitleBinding::class.java) {

    private val activityViewModel by activityViewModels<CropExaminationActivity.ViewModel>()

    override fun onResume() {
        super.onResume()

        lifecycleScope.launch(Dispatchers.Main) {  // TODO
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
                        activityViewModel.cropProcessingCoroutine.invokeOnCompletion {
                            castActivity<CropExaminationActivity>().startMainActivity()
                        }
                    }
                    .playOn(this)
            }
        }
    }
}