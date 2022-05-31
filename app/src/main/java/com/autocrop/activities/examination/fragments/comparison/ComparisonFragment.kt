package com.autocrop.activities.examination.fragments.comparison

import android.content.Context
import androidx.transition.Transition
import androidx.transition.TransitionInflater
import androidx.transition.TransitionListenerAdapter
import com.autocrop.activities.examination.fragments.ExaminationActivityFragment
import com.w2sv.autocrop.databinding.ExaminationFragmentComparisonBinding

class ComparisonFragment
    : ExaminationActivityFragment<ExaminationFragmentComparisonBinding>(ExaminationFragmentComparisonBinding::class.java){

    override fun onAttach(context: Context) {
        super.onAttach(context)

        sharedElementEnterTransition = TransitionInflater.from(requireContext())
            .inflateTransition(android.R.transition.move)
            .addListener(object: TransitionListenerAdapter(){
                private var enterTransitionCompleted = false

                override fun onTransitionEnd(transition: Transition) {
                    super.onTransitionEnd(transition)

                    if (!enterTransitionCompleted){
                        with(binding.comparisonIv){
                            showMarginalizedCrop()

                            layoutParams = binding.root.layoutParams
                            showScreenshot()
                        }
                        enterTransitionCompleted = true
                    }
                }
            })
    }

    fun prepareExitTransition() =
        binding.comparisonIv.prepareExitTransition()
}
