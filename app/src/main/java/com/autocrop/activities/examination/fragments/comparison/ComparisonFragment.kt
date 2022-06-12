package com.autocrop.activities.examination.fragments.comparison

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.RelativeLayout
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.transition.Transition
import androidx.transition.TransitionInflater
import androidx.transition.TransitionListenerAdapter
import com.autocrop.activities.examination.fragments.ExaminationActivityFragment
import com.autocrop.activities.examination.fragments.viewpager.ViewPagerViewModel
import com.autocrop.global.BooleanPreferences
import com.autocrop.utilsandroid.buildAndShow
import com.autocrop.utilsandroid.snacky
import com.w2sv.autocrop.R
import com.w2sv.autocrop.databinding.ExaminationFragmentComparisonBinding

class ComparisonFragment
    : ExaminationActivityFragment<ExaminationFragmentComparisonBinding>(ExaminationFragmentComparisonBinding::class.java){

    private lateinit var viewModel: ComparisonViewModel

    override fun onAttach(context: Context) {
        super.onAttach(context)

        viewModel = viewModels<ComparisonViewModel>{
            val viewPagerViewModel by activityViewModels<ViewPagerViewModel>()

            ComparisonViewModelFactory(
                viewPagerViewModel.dataSet.currentCropBundle
            )
        }.value

        sharedElementEnterTransition = TransitionInflater.from(context)
            .inflateTransition(android.R.transition.move)
            .addListener(object: TransitionListenerAdapter(){
                override fun onTransitionEnd(transition: Transition) {
                    super.onTransitionEnd(transition)

                    if (!viewModel.enterTransitionCompleted){
                        binding.comparisonIv.onSharedElementEnterTransitionEnd(binding.root.layoutParams as RelativeLayout.LayoutParams)
                        viewModel.enterTransitionCompleted = true

                        if (!BooleanPreferences.comparisonInstructionsShown){
                            requireActivity()
                                .snacky("Tap screen to toggle between original screenshot and crop")
                                .setIcon(R.drawable.ic_outline_info_24)
                                .buildAndShow()
                            BooleanPreferences.comparisonInstructionsShown = true
                        }
                    }
                }
            })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.displayScreenshot.observe(viewLifecycleOwner){
            binding.comparisonIv.set(it, viewModel.enterTransitionCompleted)
        }
    }

    fun prepareExitTransition(){
        binding.comparisonIv.prepareSharedElementExitTransition()
    }
}
