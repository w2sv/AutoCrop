package com.autocrop.activities.examination.fragments.viewpager

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.SpannableStringBuilder
import android.view.View
import androidx.core.text.bold
import androidx.core.text.color
import androidx.fragment.app.viewModels
import androidx.transition.TransitionInflater
import androidx.viewpager2.widget.ViewPager2
import com.autocrop.activities.examination.fragments.ExaminationActivityFragment
import com.autocrop.activities.examination.fragments.viewpager.dialogs.InstructionsDialog
import com.autocrop.collections.Crop
import com.autocrop.global.BooleanPreferences
import com.autocrop.uielements.recyclerview.CubeOutPageTransformer
import com.autocrop.uielements.view.animate
import com.autocrop.uielements.view.crossFade
import com.autocrop.uielements.view.show
import com.autocrop.utils.numericallyInflected
import com.autocrop.utilsandroid.buildAndShow
import com.autocrop.utilsandroid.getThemedColor
import com.autocrop.utilsandroid.asMutable
import com.autocrop.utilsandroid.snacky
import com.daimajia.androidanimations.library.Techniques
import com.w2sv.autocrop.R
import com.w2sv.autocrop.databinding.ExaminationFragmentViewpagerBinding

class ViewPagerFragment :
    ExaminationActivityFragment<ExaminationFragmentViewpagerBinding>(ExaminationFragmentViewpagerBinding::class.java) {

    private val viewModel by viewModels<ViewPagerViewModel>(::requireActivity)

    override fun onAttach(context: Context) {
        super.onAttach(context)

        sharedElementReturnTransition = TransitionInflater.from(context)
            .inflateTransition(android.R.transition.move)
    }

    override fun onViewCreatedCore(savedInstanceState: Bundle?) {
        super.onViewCreatedCore(savedInstanceState)

        binding.viewPager.initialize()
        viewModel.setLiveDataObservers()
    }

    private fun ViewPagerViewModel.setLiveDataObservers() {
        dataSet.currentPosition.observe(viewLifecycleOwner) { position ->
            binding.discardingStatisticsTv.updateText(position)

            dataSet.pageIndex(position).let { pageIndex ->
                val page = pageIndex + 1

                binding.pageIndicationTv.updateText(page)
                binding.pageIndicationBar.update(
                    pageIndex,
                    bouncingAnimationBlocked = autoScroll.value == true && page == dataSet.size
                )
            }
        }

        autoScroll.observe(viewLifecycleOwner) { autoScroll ->
            if (autoScroll) {
                binding.cancelAutoScrollButton.show()
                scroller = Scroller().apply {
                    run(binding.viewPager, maxAutoScrolls){
                        this@setLiveDataObservers.autoScroll.asMutable.postValue(false)
                    }
                }
            } else {
                binding.viewPager.setPageTransformer(CubeOutPageTransformer())

                val manualScrollingStateViews = arrayOf<View>(
                    binding.discardingStatisticsTv,
                    binding.menuInflationButton
                )

                scroller?.let {
                    it.cancel()
                    crossFade(
                        arrayOf(binding.cancelAutoScrollButton),
                        manualScrollingStateViews
                    )
                } ?: manualScrollingStateViews.forEach { it.show() }

                if (!BooleanPreferences.viewPagerInstructionsShown)
                    Handler(Looper.getMainLooper()).postDelayed(
                        {
                            InstructionsDialog()
                                .apply {
                                    positiveButtonOnClickListener = ::displayDismissedScreenshotsSnackbarIfApplicable
                                }
                                .show(parentFragmentManager)
                            BooleanPreferences.viewPagerInstructionsShown = true
                        },
                        resources.getInteger(R.integer.delay_minimal).toLong()
                    )
                else
                    displayDismissedScreenshotsSnackbarIfApplicable()

            }
            binding.viewPager.isUserInputEnabled = !autoScroll
        }

        dataSet.observe(viewLifecycleOwner) { dataSet ->
            if (dataSet.size == 1)
                binding.pageIndicationBar.animate(Techniques.ZoomOut)
        }
    }

    private fun displayDismissedScreenshotsSnackbarIfApplicable(){
        if (!viewModel.displayedEntrySnackbar){
            sharedViewModel.nDismissedScreenshots?.let {
                requireActivity().snacky(
                    SpannableStringBuilder()
                        .append("Couldn't find cropping bounds for")
                        .bold {
                            color(
                                requireContext().getThemedColor(R.color.accentuated_tv)
                            ) { append(" $it") }
                        }
                        .append(" image".numericallyInflected(it))
                )
                    .setIcon(R.drawable.ic_error_24)
                    .buildAndShow()
            }
            viewModel.displayedEntrySnackbar = true
        }
    }

    private fun ViewPager2.initialize() {
        adapter = CropPagerAdapter(
            this,
            viewModel,
            lastCropProcessedListener = typedActivity::invokeSubsequentFragment
        )

        registerOnPageChangeCallback(
            PageChangeHandler(
                viewModel
            )
        )

        setCurrentItem(
            viewModel.initialViewPosition(),
            false
        )
    }

    /**
     * Forward [adjustedCrop] to [viewModel].dataSet and notify viewPager.adapter
     */
    fun processAdjustedCrop(adjustedCrop: Crop) {
        viewModel.dataSet.currentCropBundle.crop = adjustedCrop

        (binding.viewPager.adapter!! as CropPagerAdapter).notifyItemChanged(
            binding.viewPager.currentItem,
            viewModel.dataSet.size
        )
    }

    /**
     * Cancel and nullify scroller if set, i.e. running
     */
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        viewModel.scroller?.cancel()
        viewModel.scroller = null
    }
}