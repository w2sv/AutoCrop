package com.w2sv.autocrop.activities.iodetermination.fragments.comparison

import android.content.Context
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.transition.Transition
import androidx.transition.TransitionInflater
import androidx.transition.TransitionListenerAdapter
import com.google.android.material.snackbar.Snackbar
import com.w2sv.autocrop.CropBundle
import com.w2sv.autocrop.R
import com.w2sv.autocrop.activities.iodetermination.fragments.IODeterminationActivityFragment
import com.w2sv.autocrop.databinding.FragmentComparisonBinding
import com.w2sv.autocrop.preferences.BooleanPreferences
import com.w2sv.autocrop.utils.android.extensions.getLong
import com.w2sv.autocrop.utils.android.extensions.hide
import com.w2sv.autocrop.utils.android.extensions.hideSystemBars
import com.w2sv.autocrop.utils.android.extensions.loadBitmap
import com.w2sv.autocrop.utils.android.extensions.postValue
import com.w2sv.autocrop.utils.android.extensions.show
import com.w2sv.autocrop.utils.android.extensions.showSystemBars
import com.w2sv.autocrop.utils.android.extensions.snackyBuilder
import com.w2sv.kotlinutils.extensions.launchDelayed

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
            ComparisonViewModel.Factory(
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
                            lifecycleScope.launchDelayed(resources.getLong(R.integer.delay_small)) {
                                viewModel.useInsetLayoutParams.postValue(false)
                                viewModel.displayScreenshot.postValue(true)

                                if (!BooleanPreferences.comparisonInstructionsShown)
                                    requireActivity()
                                        .snackyBuilder("Tap screen to toggle between the original screenshot and the crop")
                                        .setIcon(R.drawable.ic_outline_info_24)
                                        .build()
                                        .addCallback(object : Snackbar.Callback() {
                                            override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                                                super.onDismissed(transientBottomBar, event)

                                                viewModel.showButtons.postValue(true)
                                            }
                                        })
                                        .show()
                                else
                                    viewModel.showButtons.postValue(true)
                            }
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
        viewModel.showButtons.observe(viewLifecycleOwner) {
            with(binding.buttonLayout) {
                if (it)
                    show()
                else
                    hide()
            }
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

    fun onPreRemove() {
        viewModel.showButtons.postValue(false)
        viewModel.useInsetLayoutParams.postValue(true)
        viewModel.displayScreenshot.postValue(false)
    }
}