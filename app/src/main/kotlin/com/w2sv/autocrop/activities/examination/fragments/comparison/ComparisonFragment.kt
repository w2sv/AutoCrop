package com.w2sv.autocrop.activities.examination.fragments.comparison

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.os.Bundle
import android.view.View
import android.view.animation.DecelerateInterpolator
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
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
import com.w2sv.androidutils.extensions.getLong
import com.w2sv.androidutils.extensions.postValue
import com.w2sv.androidutils.extensions.remove
import com.w2sv.androidutils.extensions.show
import com.w2sv.androidutils.extensions.toggle
import com.w2sv.autocrop.R
import com.w2sv.autocrop.activities.AppFragment
import com.w2sv.autocrop.activities.examination.fragments.manualcrop.extensions.getScaleY
import com.w2sv.autocrop.activities.getFragmentInstance
import com.w2sv.autocrop.cropbundle.CropBundle
import com.w2sv.autocrop.cropbundle.io.extensions.loadBitmap
import com.w2sv.autocrop.databinding.FragmentComparisonBinding
import com.w2sv.autocrop.preferences.GlobalFlags
import com.w2sv.autocrop.utils.extensions.snackyBuilder
import com.w2sv.kotlinutils.delegates.AutoSwitch
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

@AndroidEntryPoint
class ComparisonFragment
    : AppFragment<FragmentComparisonBinding>(FragmentComparisonBinding::class.java) {

    companion object {
        fun getInstance(cropBundle: CropBundle): ComparisonFragment =
            getFragmentInstance(ComparisonFragment::class.java, CropBundle.EXTRA to cropBundle)
    }

    @Inject
    lateinit var globalFlags: GlobalFlags

    @HiltViewModel
    class ViewModel @Inject constructor(
        savedStateHandle: SavedStateHandle,
        @ApplicationContext context: Context
    ) : androidx.lifecycle.ViewModel() {

        val cropBundle: CropBundle = savedStateHandle[CropBundle.EXTRA]!!
        val screenshotBitmap: Bitmap = context.contentResolver.loadBitmap(cropBundle.screenshot.uri)!!

        val enterTransitionCompleted by AutoSwitch(false, switchOn = false)

        val displayScreenshotLive: LiveData<Boolean> = MutableLiveData(false)
        val showButtonsLive: LiveData<Boolean> = MutableLiveData(false)
        val screenshotImageViewMatrixLive: LiveData<Matrix> = MutableLiveData(null)
    }

    private val viewModel by viewModels<ViewModel>()

    override fun onAttach(context: Context) {
        super.onAttach(context)

        sharedElementEnterTransition = TransitionInflater.from(context)
            .inflateTransition(android.R.transition.move)
            .setDuration(resources.getLong(R.integer.delay_medium))
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

            if (!globalFlags.comparisonInstructionsShown)
                requireActivity()
                    .snackyBuilder("Tap screen to toggle between the original screenshot and the crop")
                    .setIcon(R.drawable.ic_info_24)
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

        with(binding.cropIv) {
            ViewCompat.setTransitionName(this, viewModel.cropBundle.identifier())
            setImageBitmap(viewModel.cropBundle.crop.bitmap)
        }
        binding.screenshotIv.setImageBitmap(viewModel.screenshotBitmap)

        binding.setOnClickListeners()
        viewModel.setLiveDataObservers()
    }

    private fun FragmentComparisonBinding.setOnClickListeners() {
        root.setOnClickListener {
            viewModel.displayScreenshotLive.toggle()
        }

        backButton.setOnClickListener {
            popFromFragmentManager((this@ComparisonFragment as Fragment).parentFragmentManager)  // to resolve AS lint issue
        }
    }

    private fun ViewModel.setLiveDataObservers() {
        screenshotImageViewMatrixLive.observe(viewLifecycleOwner) { optionalMatrix ->
            optionalMatrix?.let { matrix ->
                with(binding.cropIv) {
                    imageMatrix = matrix
                    translationY = viewModel.cropBundle.crop.edges.top.toFloat() * matrix.getScaleY()
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