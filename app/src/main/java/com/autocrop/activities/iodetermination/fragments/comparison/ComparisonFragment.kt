package com.autocrop.activities.iodetermination.fragments.comparison

import android.content.Context
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.transition.Transition
import androidx.transition.TransitionInflater
import androidx.transition.TransitionListenerAdapter
import com.autocrop.CropBundle
import com.autocrop.activities.iodetermination.fragments.IODeterminationActivityFragment
import com.autocrop.preferences.BooleanPreferences
import com.autocrop.utils.android.extensions.getLong
import com.autocrop.utils.android.extensions.hideSystemBars
import com.autocrop.utils.android.extensions.loadBitmap
import com.autocrop.utils.android.extensions.postValue
import com.autocrop.utils.android.extensions.show
import com.autocrop.utils.android.extensions.showSystemBars
import com.autocrop.utils.android.extensions.snacky
import com.w2sv.autocrop.R
import com.w2sv.autocrop.databinding.FragmentComparisonBinding

class ComparisonFragment
    : IODeterminationActivityFragment<FragmentComparisonBinding>(FragmentComparisonBinding::class.java) {

    companion object {
        fun instance(cropBundle: CropBundle): ComparisonFragment =
            ComparisonFragment()
                .apply {
                    arguments = bundleOf(
                        CropBundle.EXTRA to cropBundle
                    )
                }
    }

    val viewModel by viewModels<ComparisonViewModel> {
        @Suppress("DEPRECATION")
        requireArguments().getParcelable<CropBundle>(CropBundle.EXTRA)!!.run {
            ComparisonViewModelFactory(
                this,
                requireContext().contentResolver.loadBitmap(screenshot.uri),
                BitmapDrawable(resources, crop.bitmap)
            )
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        sharedElementEnterTransition = TransitionInflater.from(context)
            .inflateTransition(android.R.transition.move)
            .addListener(
                object : TransitionListenerAdapter() {
                    override fun onTransitionEnd(transition: Transition) {
                        super.onTransitionEnd(transition)

                        if (!viewModel.enterTransitionCompleted) {
                            Handler(Looper.getMainLooper()).postDelayed(
                                {
                                    viewModel.useInsetLayoutParams.postValue(false)
                                    viewModel.displayScreenshot.postValue(true)
                                    binding.backButton.show()
                                },
                                requireContext().resources.getLong(R.integer.delay_minimal)
                            )

                            if (!BooleanPreferences.comparisonInstructionsShown)
                                requireActivity()
                                    .snacky("Tap screen to toggle between the original screenshot and the crop")
                                    .setIcon(R.drawable.ic_outline_info_24)
                                    .show()

                            viewModel.enterTransitionCompleted = true
                        }
                    }
                }
            )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.backButton.setOnClickListener {
            onPreRemove()
            parentFragmentManager.popBackStack()
        }
    }

    override fun onResume() {
        super.onResume()

        requireActivity().hideSystemBars()
    }

    override fun onDetach() {
        super.onDetach()

        requireActivity().showSystemBars()
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)

        if (viewModel.enterTransitionCompleted)
            binding.backButton.show()
    }

    fun onPreRemove(){
        viewModel.useInsetLayoutParams.postValue(true)
        viewModel.displayScreenshot.postValue(false)
    }
}
