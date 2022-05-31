package com.autocrop.activities.examination.fragments.comparison

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.RelativeLayout
import androidx.fragment.app.viewModels
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import androidx.transition.Transition
import androidx.transition.TransitionInflater
import androidx.transition.TransitionListenerAdapter
import com.autocrop.activities.examination.fragments.ExaminationActivityFragment
import com.autocrop.uielements.view.remove
import com.w2sv.autocrop.R
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
                        binding.comparisonIv.onEnterTransitionEnd(binding.root.layoutParams as RelativeLayout.LayoutParams)
                        enterTransitionCompleted = true
                    }
                }
            })
    }

    override fun onViewCreatedCore(savedInstanceState: Bundle?) {
        val viewModel = ViewModelProvider(requireActivity())[ComparisonViewModel::class.java]

        viewModel.displayingScreenshot.observe(viewLifecycleOwner){
            binding.statusTv.text = resources.getString(if (it) R.string.original else R.string.crop)
        }
    }

    fun prepareExitTransition(){
        binding.statusTv.remove()
        binding.comparisonIv.prepareExitTransition()
    }
}
