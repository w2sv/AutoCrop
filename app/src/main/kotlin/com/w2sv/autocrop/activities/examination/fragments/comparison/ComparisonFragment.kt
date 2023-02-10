package com.w2sv.autocrop.activities.examination.fragments.comparison

import android.app.Dialog
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.os.Bundle
import android.view.View
import android.view.animation.DecelerateInterpolator
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.transition.Transition
import androidx.transition.TransitionInflater
import androidx.transition.TransitionListenerAdapter
import com.w2sv.androidutils.extensions.crossVisualize
import com.w2sv.androidutils.extensions.getColoredDrawable
import com.w2sv.androidutils.extensions.getLong
import com.w2sv.androidutils.extensions.launchDelayed
import com.w2sv.androidutils.extensions.postValue
import com.w2sv.androidutils.extensions.remove
import com.w2sv.androidutils.extensions.show
import com.w2sv.androidutils.ui.UncancelableDialogFragment
import com.w2sv.autocrop.R
import com.w2sv.autocrop.activities.AppFragment
import com.w2sv.autocrop.activities.examination.ExaminationActivity
import com.w2sv.autocrop.activities.examination.fragments.adjustment.extensions.getScaleY
import com.w2sv.autocrop.activities.getFragment
import com.w2sv.autocrop.databinding.ComparisonBinding
import com.w2sv.cropbundle.CropBundle
import com.w2sv.cropbundle.io.extensions.loadBitmap
import com.w2sv.preferences.GlobalFlags
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

@AndroidEntryPoint
class ComparisonFragment
    : AppFragment<ComparisonBinding>(ComparisonBinding::class.java),
      ComparisonInstructionsDialog.Listener {

    companion object {
        fun getInstance(cropBundlePosition: Int): ComparisonFragment =
            getFragment(ComparisonFragment::class.java, CropBundle.EXTRA_POSITION to cropBundlePosition)
    }

    @Inject
    lateinit var globalFlags: GlobalFlags

    @HiltViewModel
    class ViewModel @Inject constructor(
        savedStateHandle: SavedStateHandle,
        @ApplicationContext context: Context
    ) : androidx.lifecycle.ViewModel() {

        val cropBundle: CropBundle =
            ExaminationActivity.ViewModel.cropBundles[savedStateHandle[CropBundle.EXTRA_POSITION]!!]
        val screenshotBitmap: Bitmap = context.contentResolver.loadBitmap(cropBundle.screenshot.uri)!!

        var enterTransitionCompleted: Boolean = false
        var blockStatusTVDisplay: Boolean = false

        val displayScreenshotLive: LiveData<Boolean?> = MutableLiveData(null)
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
                            onEnterTransitionCompleted()
                    }
                }
            )
    }

    private fun onEnterTransitionCompleted() {
        launchAfterShortDelay {
            viewModel.enterTransitionCompleted = true
            viewModel.blockStatusTVDisplay = true
            viewModel.displayScreenshotLive.postValue(true)
            viewModel.showButtonsLive.postValue(true)

            if (!globalFlags.comparisonInstructionsShown)
                launchDelayed(resources.getLong(R.integer.delay_small)) {
                    ComparisonInstructionsDialog().show(childFragmentManager)
                }
            else
                unblockStatusTVDisplay()
        }
    }

    override fun onInstructionsDialogClosed() {
        unblockStatusTVDisplay()
    }

    private fun unblockStatusTVDisplay() {
        viewModel.blockStatusTVDisplay = false

        // trigger display of tv
        with(viewModel.displayScreenshotLive) {
            value?.let {
                postValue(it)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.populate()
        binding.setOnClickListeners()
        viewModel.setLiveDataObservers()
    }

    private fun ComparisonBinding.populate() {
        with(cropIv) {
            transitionName = viewModel.cropBundle.identifier()
            setImageBitmap(viewModel.cropBundle.crop.bitmap)
        }
        screenshotIv.setImageBitmap(viewModel.screenshotBitmap)
    }

    private fun ComparisonBinding.setOnClickListeners() {
        root.setOnClickListener {
            with(viewModel.displayScreenshotLive) {
                value?.let {
                    postValue(!it)
                }
            }
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
        displayScreenshotLive.observe(viewLifecycleOwner) { optional ->
            optional?.let {
                if (it)
                    crossVisualize(binding.cropIv, binding.screenshotIv)
                else
                    crossVisualize(binding.screenshotIv, binding.cropIv)

                if (!viewModel.blockStatusTVDisplay)
                    binding.ivStatusTv.setTextAndShow(it)
            }
        }
        showButtonsLive.observe(viewLifecycleOwner) {
            with(binding.backButton) {
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

class ComparisonInstructionsDialog : UncancelableDialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        builder()
            .apply {
                setTitle("Comparison Screen")
                setIcon(
                    context.getColoredDrawable(
                        R.drawable.ic_image_search_24,
                        com.w2sv.common.R.color.magenta_saturated
                    )
                )
                setMessage("Tap screen to toggle between the original screenshot and the crop \uD83D\uDC47")
                setPositiveButton("Got it!") { _, _ -> (parentFragment as Listener).onInstructionsDialogClosed() }
            }
            .create()

    interface Listener {
        fun onInstructionsDialogClosed()
    }
}