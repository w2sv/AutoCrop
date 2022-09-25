package com.autocrop.activities.examination.fragments.comparison

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.widget.RelativeLayout
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.transition.Transition
import androidx.transition.TransitionInflater
import androidx.transition.TransitionListenerAdapter
import com.autocrop.activities.examination.fragments.ExaminationActivityFragment
import com.autocrop.activities.examination.fragments.croppager.viewmodel.ViewPagerViewModel
import com.autocrop.preferences.BooleanPreferences
import com.autocrop.utils.android.livedata.asMutable
import com.autocrop.utils.android.buildAndShow
import com.autocrop.utils.android.snacky
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
                viewPagerViewModel.dataSet.currentValue
            )
        }.value

        sharedElementEnterTransition = TransitionInflater.from(context)
            .inflateTransition(android.R.transition.move)
            .addListener(object: TransitionListenerAdapter(){
                override fun onTransitionEnd(transition: Transition) {
                    super.onTransitionEnd(transition)

                    if (!viewModel.enterTransitionCompleted){
                        Handler(Looper.getMainLooper()).postDelayed(
                            {
                                viewModel.enterTransitionCompleted = true
                                binding.comparisonIv.layoutParams = binding.root.layoutParams as RelativeLayout.LayoutParams
                                viewModel.displayScreenshot.asMutable.postValue(true)
                            },
                            50
                        )

                        if (!BooleanPreferences.comparisonInstructionsShown)
                            requireActivity()
                                .snacky("Tap screen to toggle between original screenshot and crop")
                                .setIcon(R.drawable.ic_outline_info_24)
                                .buildAndShow()
                    }
                }
            })
    }

    fun prepareExitTransition(){
        binding.comparisonIv.prepareSharedElementExitTransition()
    }
}
