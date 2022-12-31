package com.w2sv.autocrop.activities.cropexamination.fragments.comparison

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.os.Bundle
import android.view.View
import android.view.animation.DecelerateInterpolator
import androidx.core.os.bundleOf
import androidx.core.view.ViewCompat
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.transition.Transition
import androidx.transition.TransitionInflater
import androidx.transition.TransitionListenerAdapter
import com.google.android.material.snackbar.Snackbar
import com.w2sv.androidutils.extensions.crossVisualize
import com.w2sv.androidutils.extensions.postValue
import com.w2sv.androidutils.extensions.remove
import com.w2sv.androidutils.extensions.show
import com.w2sv.androidutils.extensions.toggle
import com.w2sv.autocrop.R
import com.w2sv.autocrop.activities.ApplicationFragment
import com.w2sv.autocrop.activities.cropexamination.fragments.manualcrop.utils.extensions.getScaleY
import com.w2sv.autocrop.cropbundle.CropBundle
import com.w2sv.autocrop.cropbundle.io.extensions.loadBitmap
import com.w2sv.autocrop.databinding.FragmentComparisonBinding
import com.w2sv.autocrop.preferences.BooleanPreferences
import com.w2sv.autocrop.utils.extensions.snackyBuilder
import com.w2sv.kotlinutils.delegates.AutoSwitch
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

@AndroidEntryPoint
class ComparisonFragment
    : ApplicationFragment<FragmentComparisonBinding>(FragmentComparisonBinding::class.java) {

    companion object {
        fun getInstance(cropBundle: CropBundle): ComparisonFragment =
            ComparisonFragment()
                .apply {
                    arguments = bundleOf(
                        CropBundle.EXTRA to cropBundle
                    )
                }
    }

    @Inject
    lateinit var booleanPreferences: BooleanPreferences

    @HiltViewModel
    class ViewModel @Inject constructor(
        savedStateHandle: SavedStateHandle,
        @ApplicationContext context: Context
    ) : androidx.lifecycle.ViewModel() {

        val cropBundle: CropBundle = savedStateHandle[CropBundle.EXTRA]!!
        val screenshotBitmap: Bitmap = context.contentResolver.loadBitmap(cropBundle.screenshot.uri)!!

        val enterTransitionCompleted by AutoSwitch(false, switchOn = false)

        val displayScreenshotLive: LiveData<Boolean> by lazy {
            MutableLiveData(false)
        }
        val showButtonsLive: LiveData<Boolean> by lazy {
            MutableLiveData(false)
        }
        val screenshotImageViewMatrixLive: LiveData<Matrix> by lazy {
            MutableLiveData(null)
        }
    }

    private val viewModel by viewModels<ViewModel>()

    override fun onAttach(context: Context) {
        super.onAttach(context)

        sharedElementEnterTransition = TransitionInflater.from(context)
            .inflateTransition(android.R.transition.move)
            .setDuration(500)
            .setInterpolator(DecelerateInterpolator(1.5f))
            .addListener(
                object : TransitionListenerAdapter() {
                    override fun onTransitionEnd(transition: Transition) {
                        super.onTransitionEnd(transition)

                        if (!viewModel.enterTransitionCompleted)
                            onEnterTransition()
                    }
                }
            )
    }

    private fun onEnterTransition() {
        launchAfterShortDelay {
            viewModel.displayScreenshotLive.postValue(true)

            if (!booleanPreferences.comparisonInstructionsShown)
                requireActivity()
                    .snackyBuilder("Tap screen to toggle between the original screenshot and the crop")
                    .setIcon(R.drawable.ic_outline_info_24)
                    .build()
                    .addCallback(object : Snackbar.Callback() {
                        override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                            super.onDismissed(transientBottomBar, event)

                            viewModel.showButtonsLive.postValue(true)
                        }
                    })
                    .show()
            else
                viewModel.showButtonsLive.postValue(true)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.initialize()
        viewModel.setLiveDataObservers()
    }

    private fun FragmentComparisonBinding.initialize() {
        with(cropIv) {
            ViewCompat.setTransitionName(this, viewModel.cropBundle.identifier())
            setImageBitmap(viewModel.cropBundle.crop.bitmap)
        }

        screenshotIv.setImageBitmap(viewModel.screenshotBitmap)

        root.setOnClickListener {
            viewModel.displayScreenshotLive.toggle()
        }

        backButton.setOnClickListener {
            popFromFragmentManager(parentFragmentManager)
        }
    }

    private fun ViewModel.setLiveDataObservers() {
        screenshotImageViewMatrixLive.observe(viewLifecycleOwner) {
            it?.let {
                with(binding.cropIv) {
                    imageMatrix = it
                    translationY = viewModel.cropBundle.crop.edges.top.toFloat() * it.getScaleY()
                    postInvalidate()
                }
            }
        }
        displayScreenshotLive.observe(viewLifecycleOwner) {
            if (it)
                crossVisualize(binding.cropIv, binding.screenshotIv)
            else
                crossVisualize(binding.screenshotIv, binding.cropIv)

            binding.ivStatusTv.setTextAndShow(it)
        }
        showButtonsLive.observe(viewLifecycleOwner) {
            with(binding.buttonLayout) {
                if (it)
                    show()
                else
                    remove()
            }
        }
    }

    fun popFromFragmentManager(fragmentManager: FragmentManager) {
        onPreRemove()
        fragmentManager.popBackStack()
    }

    private fun onPreRemove() {
        with(viewModel) {
            showButtonsLive.postValue(false)
            binding.cropIv.show()
        }
    }
}