package com.w2sv.autocrop.activities.iodetermination.fragments.croppager

import android.content.Context
import android.os.Bundle
import android.text.SpannableStringBuilder
import androidx.core.text.bold
import androidx.core.text.color
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.transition.TransitionInflater
import com.daimajia.androidanimations.library.Techniques
import com.w2sv.autocrop.Crop
import com.w2sv.autocrop.CropEdges
import com.w2sv.autocrop.R
import com.w2sv.autocrop.activities.iodetermination.fragments.IODeterminationActivityFragment
import com.w2sv.autocrop.activities.iodetermination.fragments.croppager.dialogs.CropDialog
import com.w2sv.autocrop.activities.iodetermination.fragments.croppager.dialogs.CropEntiretyDialog
import com.w2sv.autocrop.activities.iodetermination.fragments.croppager.dialogs.CropPagerInstructionsDialog
import com.w2sv.autocrop.activities.iodetermination.fragments.croppager.pager.CropPagerAdapter
import com.w2sv.autocrop.activities.iodetermination.fragments.croppager.pager.CropPagerProxy
import com.w2sv.autocrop.activities.iodetermination.fragments.croppager.viewmodel.CropPagerViewModel
import com.w2sv.autocrop.activities.iodetermination.fragments.croppager.viewmodel.Scroller
import com.w2sv.autocrop.activities.iodetermination.fragments.manualcrop.ManualCropFragment
import com.w2sv.autocrop.activities.iodetermination.fragments.saveall.SaveAllFragment
import com.w2sv.autocrop.databinding.FragmentCroppagerBinding
import com.w2sv.autocrop.preferences.BooleanPreferences
import com.w2sv.autocrop.utils.android.BackPressHandler
import com.w2sv.autocrop.utils.android.extensions.animate
import com.w2sv.autocrop.utils.android.extensions.crossFade
import com.w2sv.autocrop.utils.android.extensions.getLong
import com.w2sv.autocrop.utils.android.extensions.getThemedColor
import com.w2sv.autocrop.utils.android.extensions.loadBitmap
import com.w2sv.autocrop.utils.android.extensions.postValue
import com.w2sv.autocrop.utils.android.extensions.show
import com.w2sv.autocrop.utils.android.extensions.snackyBuilder
import com.w2sv.autocrop.utils.android.postDelayed
import com.w2sv.kotlinutils.extensions.executeAsyncTask
import com.w2sv.kotlinutils.extensions.numericallyInflected
import de.mateware.snacky.Snacky

class CropPagerFragment :
    IODeterminationActivityFragment<FragmentCroppagerBinding>(FragmentCroppagerBinding::class.java),
    CropDialog.ResultListener,
    CropEntiretyDialog.ResultListener,
    ManualCropFragment.ResultListener,
    CropPagerInstructionsDialog.OnDismissedListener {

    private val viewModel by viewModels<CropPagerViewModel>()

    override fun onAttach(context: Context) {
        super.onAttach(context)

        sharedElementReturnTransition = TransitionInflater.from(context)
            .inflateTransition(android.R.transition.move)
    }

    private lateinit var viewPagerProxy: CropPagerProxy
    lateinit var handleBackPress: BackPressHandler

    override fun onViewCreatedCore(savedInstanceState: Bundle?) {
        super.onViewCreatedCore(savedInstanceState)

        viewPagerProxy = CropPagerProxy(
            binding.viewPager,
            viewModel
        )
        viewModel.setLiveDataObservers()

        handleBackPress = BackPressHandler(
            requireActivity().snackyBuilder(
                "Tap again to return to main screen"
            )
                .setView()
        ) {
            castActivity.startMainActivity()
        }
    }

    private fun CropPagerViewModel.setLiveDataObservers() {
        dataSet.currentPosition.observe(viewLifecycleOwner) { position ->
            binding.discardingStatisticsTv.update(position)

            dataSet.pageIndex(position).let { pageIndex ->
                binding.pageIndicationTv.update(pageIndex + 1)
                binding.pageIndicationBar.update(pageIndex)
            }
        }

        liveAutoScroll.observe(viewLifecycleOwner) { autoScroll ->
            if (autoScroll) {
                binding.cancelAutoScrollButton.show()
                scroller = Scroller().apply {
                    run(binding.viewPager, autoScrolls) {
                        this@setLiveDataObservers.liveAutoScroll.postValue(false)
                    }
                }
            }
            else {
                binding.viewPager.setPageTransformer { page, position ->
                    with(page) {
                        pivotX = (if (position < 0) width else 0).toFloat()
                        pivotY = height * 0.5f
                        rotationY = 90f * position
                    }
                }

                scroller?.let { scroller ->
                    scroller.cancel()
                    crossFade(
                        binding.cancelAutoScrollButton,
                        binding.bottomElements
                    )
                }
                    ?: binding.bottomElements.show()

                if (!BooleanPreferences.cropPagerInstructionsShown)
                    postDelayed(resources.getLong(R.integer.delay_small)) {
                        CropPagerInstructionsDialog()
                            .show(childFragmentManager)
                    }
                else
                    onAutoScrollConcluded()

            }
            binding.viewPager.isUserInputEnabled = !autoScroll
        }

        dataSet.observe(viewLifecycleOwner) { dataSet ->
            if (dataSet.size == 1)
                binding.pageIndicationBar.animate(Techniques.ZoomOut)
        }
    }

    override fun onDismissed() {
        onAutoScrollConcluded()
    }

    private fun onAutoScrollConcluded() {
        with(sharedViewModel) {
            if (!showedDismissedScreenshotsSnackbar && nDismissedScreenshots != 0) {
                requireActivity().snackyBuilder(
                    SpannableStringBuilder()
                        .append("Couldn't find crop bounds for")
                        .bold {
                            color(
                                requireContext().getThemedColor(R.color.magenta_bright)
                            ) { append(" $nDismissedScreenshots") }
                        }
                        .append(" image".numericallyInflected(nDismissedScreenshots))
                )
                    .setView()
                    .setIcon(R.drawable.ic_error_24)
                    .build().show()
            }
        }
    }

    override fun onResult(cropEdges: CropEdges) {
        processAdjustedCropEdges(cropEdges)
        postDelayed(resources.getLong(R.integer.delay_small)) {
            requireActivity().snackyBuilder(
                "Adjusted crop"
            )
                .setView()
                .setIcon(R.drawable.ic_check_green_24)
                .build().show()
        }
    }

    /**
     * Set new [Crop] in [viewModel].dataSet.currentPosition and notify [CropPagerAdapter]
     */
    private fun processAdjustedCropEdges(adjustedEdges: CropEdges) {
        with(viewModel.dataSet.currentElement) {
            crop = Crop.fromScreenshot(
                requireContext().contentResolver.loadBitmap(screenshot.uri),
                screenshot.mediaStoreData.diskUsage,
                adjustedEdges
            )
        }

        (binding.viewPager.adapter!! as CropPagerAdapter).notifyItemChanged(
            binding.viewPager.currentItem,
            viewModel.dataSet.size
        )
    }

    /**
     * Increment nSavedCrops if applicable
     *
     * triggers activity exit if [viewModel].dataSet about to be exhausted OR
     * hide pageIndicationSeekBar AND/OR
     * removes view, procedure action has been selected for, from pager
     */
    override fun onResult(confirmed: Boolean, dataSetPosition: Int) {
        if (confirmed)
            with(sharedViewModel) {
                singularCropSavingJob = lifecycleScope.executeAsyncTask(
                    makeCropBundleProcessor(
                        dataSetPosition,
                        BooleanPreferences.deleteScreenshots,
                        requireContext().contentResolver
                    )
                )
            }

        if (viewModel.dataSet.size == 1)
            castActivity.invokeSubsequentFragment()
        else
            viewPagerProxy.removeView(dataSetPosition)
    }

    override fun onResult(confirmed: Boolean) {
        if (confirmed)
            fragmentHostingActivity
                .fragmentReplacementTransaction(SaveAllFragment(), true)
                .commit()
        else
            castActivity.invokeSubsequentFragment()
    }

    private fun Snacky.Builder.setView(): Snacky.Builder =
        setView(binding.snackbarRepelledHostingLayout)

    /**
     * Cancel and nullify scroller if set, i.e. running
     */
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        viewModel.scroller?.cancel()
        viewModel.scroller = null
    }
}