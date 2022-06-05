package com.autocrop.activities.examination.fragments.comparison

import android.content.Context
import android.os.Bundle
import android.widget.RelativeLayout
import androidx.core.view.ViewCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.transition.Transition
import androidx.transition.TransitionInflater
import androidx.transition.TransitionListenerAdapter
import com.autocrop.activities.examination.fragments.ExaminationActivityFragment
import com.autocrop.activities.examination.fragments.viewpager.ViewPagerViewModel
import com.autocrop.activities.examination.fragments.viewpager.transitionName
import com.autocrop.utilsandroid.buildAndShow
import com.autocrop.utilsandroid.snacky
import com.w2sv.autocrop.databinding.ExaminationFragmentComparisonBinding

class ComparisonFragment
    : ExaminationActivityFragment<ExaminationFragmentComparisonBinding>(ExaminationFragmentComparisonBinding::class.java){

    private val viewModel by lazy {
        ViewModelProvider(
            this,
            ComparisonViewModelFactory(
                ViewModelProvider(activity as ViewModelStoreOwner)[ViewPagerViewModel::class.java].dataSet.currentCropBundle
            )
        )[ComparisonViewModel::class.java]
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        val transition = TransitionInflater.from(context)
            .inflateTransition(android.R.transition.move)
            .addListener(object: TransitionListenerAdapter(){
                private var enterTransitionCompleted = false

                override fun onTransitionStart(transition: Transition) {
                    super.onTransitionStart(transition)

                    println("transition start")
                }

                override fun onTransitionEnd(transition: Transition) {
                    super.onTransitionEnd(transition)

                    if (!enterTransitionCompleted){
                        binding.comparisonIv.onSharedElementEnterTransitionEnd(binding.root.layoutParams as RelativeLayout.LayoutParams)
                        enterTransitionCompleted = true
                    }
                }
            })
        sharedElementEnterTransition = transition
        sharedElementReturnTransition = transition
    }

    override fun onViewCreatedCore(savedInstanceState: Bundle?) {
        ViewCompat.setTransitionName(binding.comparisonIv, viewModel.cropBundle.transitionName())
        println("transitionName ComparisonFragment: ${viewModel.cropBundle.transitionName()}")

        if (ComparisonViewModel.displayInstructionSnackbar){
            requireActivity()
                .snacky("Tap screen to toggle between original screenshot and crop")
                .buildAndShow()
            ComparisonViewModel.displayInstructionSnackbar = false
        }
    }

    fun prepareExitTransition(){
        binding.comparisonIv.prepareExitTransition()
    }
}
