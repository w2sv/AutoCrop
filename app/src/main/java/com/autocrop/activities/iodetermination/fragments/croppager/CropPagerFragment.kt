package com.autocrop.activities.iodetermination.fragments.croppager

import android.content.Context
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.view.View
import androidx.core.text.bold
import androidx.core.text.color
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.transition.TransitionInflater
import com.autocrop.Crop
import com.autocrop.CropEdges
import com.autocrop.activities.iodetermination.fragments.IODeterminationActivityFragment
import com.autocrop.activities.iodetermination.fragments.apptitle.AppTitleFragment
import com.autocrop.activities.iodetermination.fragments.croppager.dialogs.AbstractCropDialog
import com.autocrop.activities.iodetermination.fragments.croppager.dialogs.CropDialog
import com.autocrop.activities.iodetermination.fragments.croppager.dialogs.CropEntiretyDialog
import com.autocrop.activities.iodetermination.fragments.croppager.dialogs.CropPagerInstructionsDialog
import com.autocrop.activities.iodetermination.fragments.croppager.pager.CropPagerAdapter
import com.autocrop.activities.iodetermination.fragments.croppager.pager.CropPagerProxy
import com.autocrop.activities.iodetermination.fragments.croppager.viewmodel.CropPagerViewModel
import com.autocrop.activities.iodetermination.fragments.croppager.viewmodel.Scroller
import com.autocrop.activities.iodetermination.fragments.manualcrop.ManualCropFragment
import com.autocrop.activities.iodetermination.fragments.saveall.SaveAllFragment
import com.autocrop.preferences.BooleanPreferences
import com.autocrop.utils.android.BackPressHandler
import com.autocrop.utils.android.extensions.animate
import com.autocrop.utils.android.extensions.crossFade
import com.autocrop.utils.android.extensions.getLong
import com.autocrop.utils.android.extensions.getThemedColor
import com.autocrop.utils.android.extensions.loadBitmap
import com.autocrop.utils.android.extensions.postValue
import com.autocrop.utils.android.extensions.show
import com.autocrop.utils.android.extensions.buildAndShow
import com.autocrop.utils.android.extensions.snackyBuilder
import com.autocrop.utils.android.postDelayed
import com.autocrop.utils.kotlin.extensions.executeAsyncTask
import com.autocrop.utils.kotlin.extensions.numericallyInflected
import com.daimajia.androidanimations.library.Techniques
import com.w2sv.autocrop.R
import com.w2sv.autocrop.databinding.FragmentCroppagerBinding
import de.mateware.snacky.Snacky

class CropPagerFragment :
    IODeterminationActivityFragment<FragmentCroppagerBinding>(FragmentCroppagerBinding::class.java) {

    private val viewModel by viewModels<CropPagerViewModel>()
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
        setManualCropResultListener()
    }

    private fun setManualCropResultListener() {
        setFragmentResultListener(ManualCropFragment.KEY_RESULT) {
            processAdjustedCropEdges(ManualCropFragment.getAdjustedCropEdges(it))
            postDelayed(resources.getLong(R.integer.delay_small)) {
                requireActivity().snackyBuilder(
                    "Adjusted crop"
                )
                    .setView()
                    .setIcon(R.drawable.ic_check_green_24)
                    .buildAndShow()
            }
        }
    }

    /**
     * Increment nSavedCrops if applicable
     *
     * triggers activity exit if [viewModel].dataSet about to be exhausted OR
     * hide pageIndicationSeekBar AND/OR
     * removes view, procedure action has been selected for, from pager
     */
    private fun setCropDialogResultListener() {
        setFragmentResultListener(CropDialog.RESULT_REQUEST_KEY) { bundle ->
            val dataSetPosition = bundle.getInt(CropDialog.DATA_SET_POSITION_BUNDLE_ARG_KEY)
            val saveCrop = bundle.getBoolean(AbstractCropDialog.EXTRA_DIALOG_CONFIRMED)

            if (saveCrop)
                sharedViewModel.singularCropSavingJob = lifecycleScope.executeAsyncTask(
                    sharedViewModel.makeCropBundleProcessor(
                        dataSetPosition,
                        BooleanPreferences.deleteScreenshots,
                        requireContext().contentResolver
                    )
                )

            if (viewModel.dataSet.size == 1)
                typedActivity.invokeSubsequentFragment()
            else
                viewPagerProxy.removeView(dataSetPosition)
        }
    }

    private fun setCropEntiretyDialogResultListener() {
        setFragmentResultListener(CropEntiretyDialog.RESULT_REQUEST_KEY) {
            if (it.getBoolean(AbstractCropDialog.EXTRA_DIALOG_CONFIRMED))
                fragmentHostingActivity
                    .fragmentReplacementTransaction(SaveAllFragment(), true)
                    .commit()
            else
                typedActivity.invokeSubsequentFragment()
        }
    }

    private fun setFragmentResultListener(requestKey: String, listener: (Bundle) -> Unit) {
        parentFragmentManager.setFragmentResultListener(requestKey, requireActivity()) { _, bundle ->
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

        handleBackPress = BackPressHandler(
            requireActivity().snackyBuilder(
                "Tap again to return to main screen"
            )
                .setView(),
            typedActivity::startMainActivity
        )
    }

    private fun CropPagerViewModel.setLiveDataObservers() {
        dataSet.currentPosition.observe(viewLifecycleOwner) { position ->
            binding.discardingStatisticsTv.update(position)

            dataSet.pageIndex(position).let { pageIndex ->
                binding.pageIndicationTv.update(pageIndex + 1)
                binding.pageIndicationBar.update(pageIndex)
            }
        }

        autoScroll.observe(viewLifecycleOwner) {
            if (it) {
                binding.cancelAutoScrollButton.show()
                scroller = Scroller().apply {
                    run(binding.viewPager, maxAutoScrolls) {
                        this@setLiveDataObservers.autoScroll.postValue(false)
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

                val manualScrollingStateViews = arrayOf(
                    binding.discardingStatisticsTv,
                    binding.comparisonButton as View,
                    binding.manualCropButton as View
                )

                scroller?.let { scroller ->
                    scroller.cancel()
                    crossFade(
                        arrayOf(binding.cancelAutoScrollButton),
                        manualScrollingStateViews
                    )
                }
                    ?: manualScrollingStateViews.forEach { it.show() }

                if (!BooleanPreferences.cropPagerInstructionsShown)
                    postDelayed(resources.getLong(R.integer.delay_small)) {
                        CropPagerInstructionsDialog()
                            .apply {
                                positiveButtonOnClickListener = ::displayDismissedScreenshotsSnackbarIfApplicable
                            }
                            .show(parentFragmentManager)
                    }
                else
                    displayDismissedScreenshotsSnackbarIfApplicable()

            }
            binding.viewPager.isUserInputEnabled = !it
        }

        dataSet.observe(viewLifecycleOwner) { dataSet ->
            if (dataSet.size == 1)
                binding.pageIndicationBar.animate(Techniques.ZoomOut)
        }
    }

    private fun displayDismissedScreenshotsSnackbarIfApplicable() {
        with(sharedViewModel) {
            if (!showedDismissedScreenshotsSnackbar && nDismissedScreenshots != 0) {
                requireActivity().snackyBuilder(
                    SpannableStringBuilder()
                        .append("Couldn't find cropping bounds for")
                        .bold {
                            color(
                                requireContext().getThemedColor(R.color.magenta_bright)
                            ) { append(" $nDismissedScreenshots") }
                        }
                        .append(" image".numericallyInflected(nDismissedScreenshots))
                )
                    .setView()
                    .setIcon(R.drawable.ic_error_24)
                    .buildAndShow()
            }
        }
    }

    /**
     * Block backPress throughout if either [SaveAllFragment] or [AppTitleFragment] showing,
     * otherwise return to MainActivity after confirmation
     */
    lateinit var handleBackPress: BackPressHandler

    private fun Snacky.Builder.setView(): Snacky.Builder =
        setView(binding.snackbarRepelledHostingLayout)

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
     * Cancel and nullify scroller if set, i.e. running
     */
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        viewModel.scroller?.cancel()
        viewModel.scroller = null
    }
}