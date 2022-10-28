package com.autocrop.activities.iodetermination.fragments.comparison

import android.content.Context
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
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
import com.autocrop.utils.android.extensions.hide
import com.autocrop.utils.android.extensions.hideSystemBars
import com.autocrop.utils.android.extensions.loadBitmap
import com.autocrop.utils.android.extensions.postValue
import com.autocrop.utils.android.extensions.show
import com.autocrop.utils.android.extensions.showSystemBars
import com.autocrop.utils.android.extensions.snackyBuilder
import com.autocrop.utils.android.postDelayed
import com.autocrop.utils.kotlin.delegates.AutoSwitch
import com.google.android.material.snackbar.Snackbar
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
                    private var enterTransitionCompleted by AutoSwitch(false, switchOn = false)

                    override fun onTransitionEnd(transition: Transition) {
                        super.onTransitionEnd(transition)

                        if (!enterTransitionCompleted) {
                            postDelayed(resources.getLong(R.integer.delay_small)) {
                                viewModel.useInsetLayoutParams.postValue(false)
                                viewModel.displayScreenshot.postValue(true)

                                if (BooleanPreferences.comparisonInstructionsShown)
                                    viewModel.showButtons.postValue(true)
                                else
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
            if (it)
                binding.buttonLayout.show()
            else
                binding.buttonLayout.hide()
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