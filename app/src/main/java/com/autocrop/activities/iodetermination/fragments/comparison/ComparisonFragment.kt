package com.autocrop.activities.iodetermination.fragments.comparison

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.widget.RelativeLayout
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.transition.Transition
import androidx.transition.TransitionInflater
import androidx.transition.TransitionListenerAdapter
import com.autocrop.CropBundle
import com.autocrop.activities.iodetermination.fragments.IODeterminationActivityFragment
import com.autocrop.preferences.BooleanPreferences
import com.autocrop.utils.android.extensions.show
import com.autocrop.utils.android.extensions.snacky
import com.autocrop.utils.android.livedata.asMutable
import com.w2sv.autocrop.R
import com.w2sv.autocrop.databinding.FragmentComparisonBinding

class ComparisonFragment
    : IODeterminationActivityFragment<FragmentComparisonBinding>(FragmentComparisonBinding::class.java){

    companion object{
        fun instance(cropBundle: CropBundle): ComparisonFragment =
            ComparisonFragment()
                .apply {
                    arguments = bundleOf(
                        CropBundle.EXTRA to cropBundle
                    )
                }
    }

    private lateinit var viewModel: ComparisonViewModel

    override fun onAttach(context: Context) {
        super.onAttach(context)

        viewModel = viewModels<ComparisonViewModel>{
            @Suppress("DEPRECATION")
            ComparisonViewModelFactory(
                requireArguments().getParcelable(CropBundle.EXTRA)!!
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
                                .show()
                    }
                }
            })
    }
}
