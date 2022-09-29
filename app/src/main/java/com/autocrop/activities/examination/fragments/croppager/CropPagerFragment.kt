package com.autocrop.activities.examination.fragments.croppager

import android.content.Context
import android.graphics.Rect
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.SpannableStringBuilder
import android.view.View
import androidx.core.text.bold
import androidx.core.text.color
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.transition.TransitionInflater
import com.autocrop.activities.examination.fragments.ExaminationActivityFragment
import com.autocrop.activities.examination.fragments.croppager.dialogs.AbstractCropDialog
import com.autocrop.activities.examination.fragments.croppager.dialogs.CropDialog
import com.autocrop.activities.examination.fragments.croppager.dialogs.CropEntiretyDialog
import com.autocrop.activities.examination.fragments.croppager.dialogs.InstructionsDialog
import com.autocrop.activities.examination.fragments.croppager.pager.CropPagerAdapter
import com.autocrop.activities.examination.fragments.croppager.pager.CropPagerProxy
import com.autocrop.activities.examination.fragments.croppager.viewmodel.Scroller
import com.autocrop.activities.examination.fragments.croppager.viewmodel.ViewPagerViewModel
import com.autocrop.activities.examination.fragments.saveall.SaveAllFragment
import com.autocrop.dataclasses.Crop
import com.autocrop.preferences.BooleanPreferences
import com.autocrop.ui.elements.recyclerview.CubeOutPageTransformer
import com.autocrop.ui.elements.view.animate
import com.autocrop.ui.elements.view.crossFade
import com.autocrop.ui.elements.view.show
import com.autocrop.utils.android.extensions.getThemedColor
import com.autocrop.utils.android.extensions.show
import com.autocrop.utils.android.extensions.snacky
import com.autocrop.utils.android.livedata.asMutable
import com.autocrop.utils.kotlin.extensions.executeAsyncTask
import com.autocrop.utils.kotlin.extensions.numericallyInflected
import com.daimajia.androidanimations.library.Techniques
import com.w2sv.autocrop.R
import com.w2sv.autocrop.databinding.FragmentCroppagerBinding

class CropPagerFragment :
    ExaminationActivityFragment<FragmentCroppagerBinding>(FragmentCroppagerBinding::class.java) {

    private val viewModel by viewModels<ViewPagerViewModel>(::requireActivity)
    private lateinit var viewPagerProxy: CropPagerProxy

    override fun onAttach(context: Context) {
        super.onAttach(context)

        sharedElementReturnTransition = TransitionInflater.from(context)
            .inflateTransition(android.R.transition.move)
    }

    override fun onStart() {
        super.onStart()

        setCropDialogResultListener()
        setCropEntiretyDialogResultListener()
    }

    /**
     * Increment nSavedCrops if applicable
     *
     * triggers activity exit if [viewModel].dataSet about to be exhausted OR
     * hide pageIndicationSeekBar AND/OR
     * removes view, procedure action has been selected for, from pager
     */
    private fun setCropDialogResultListener(){
        setFragmentResultListener(CropDialog.RESULT_REQUEST_KEY){ bundle ->
            val dataSetPosition = bundle.getInt(CropDialog.DATA_SET_POSITION_BUNDLE_ARG_KEY)
            val saveCrop = bundle.getBoolean(AbstractCropDialog.CONFIRMED_BUNDLE_ARG_KEY)

            if (saveCrop)
                sharedViewModel.singularCropSavingJob = lifecycleScope.executeAsyncTask(
                    sharedViewModel.makeCropBundleProcessor(
                        dataSetPosition,
                        BooleanPreferences.deleteScreenshots,
                        requireContext()
                    )
                )

            if (viewModel.dataSet.size == 1)
                typedActivity.invokeSubsequentFragment()
            else
                viewPagerProxy.removeView(dataSetPosition)
        }
    }

    private fun setCropEntiretyDialogResultListener(){
        setFragmentResultListener(CropEntiretyDialog.RESULT_REQUEST_KEY){
            if (it.getBoolean(AbstractCropDialog.CONFIRMED_BUNDLE_ARG_KEY))
                fragmentHostingActivity.replaceCurrentFragmentWith(SaveAllFragment(),true)
            else
                typedActivity.returnToMainActivity()
        }
    }

    private fun setFragmentResultListener(requestKey: String, listener: (Bundle) -> Unit){
        parentFragmentManager.setFragmentResultListener(requestKey, requireActivity()){ _, bundle ->
            listener(bundle)
        }
    }

    override fun onViewCreatedCore(savedInstanceState: Bundle?) {
        super.onViewCreatedCore(savedInstanceState)

        viewPagerProxy = CropPagerProxy(
            binding.viewPager,
            viewModel
        )
            .apply {
                setInitialView()
            }
        viewModel.setLiveDataObservers()
    }

    private fun ViewPagerViewModel.setLiveDataObservers() {
        dataSet.currentPosition.observe(viewLifecycleOwner) { position ->
            binding.discardingStatisticsTv.update(position)

            dataSet.pageIndex(position).let { pageIndex ->
                val page = pageIndex + 1

                binding.pageIndicationTv.update(page)
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
        with(sharedViewModel) {
            if (!displayedDismissedScreenshotsSnackbar && nDismissedScreenshots != 0){
                requireActivity().snacky(
                    SpannableStringBuilder()
                        .append("Couldn't find cropping bounds for")
                        .bold {
                            color(
                                requireContext().getThemedColor(R.color.accentuated_tv)
                            ) { append(" $nDismissedScreenshots") }
                        }
                        .append(" image".numericallyInflected(nDismissedScreenshots))
                )
                    .setIcon(R.drawable.ic_error_24)
                    .show()
            }
        }
    }

    /**
     * Set new [Crop] in [viewModel].dataSet.currentPosition and notify [CropPagerAdapter]
     */
    fun processAdjustedCropRect(adjustedRect: Rect) {
        with(viewModel.dataSet.currentValue) {
            crop = Crop.fromScreenshot(
                screenshot.bitmap(requireContext().contentResolver),
                screenshot.diskUsage,
                adjustedRect
            )
        }

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