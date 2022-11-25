package com.w2sv.autocrop.activities.cropexamination.fragments.croppager

import android.content.Context
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.view.View
import androidx.core.text.bold
import androidx.core.text.color
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.transition.TransitionInflater
import com.daimajia.androidanimations.library.Techniques
import com.w2sv.androidutils.extensions.getColoredIcon
import com.w2sv.androidutils.extensions.getLong
import com.w2sv.androidutils.extensions.getThemedColor
import com.w2sv.androidutils.extensions.launch
import com.w2sv.androidutils.extensions.launchDelayed
import com.w2sv.androidutils.extensions.postValue
import com.w2sv.androidutils.extensions.show
import com.w2sv.autocrop.R
import com.w2sv.autocrop.activities.cropexamination.CropExaminationActivity
import com.w2sv.autocrop.activities.cropexamination.CropExaminationActivityViewModel
import com.w2sv.autocrop.activities.cropexamination.fragments.croppager.dialogs.CropDialog
import com.w2sv.autocrop.activities.cropexamination.fragments.croppager.dialogs.CropEntiretyDialog
import com.w2sv.autocrop.activities.cropexamination.fragments.croppager.dialogs.CropPagerInstructionsDialog
import com.w2sv.autocrop.activities.cropexamination.fragments.croppager.viewmodel.CropPagerViewModel
import com.w2sv.autocrop.activities.cropexamination.fragments.croppager.viewmodel.Scroller
import com.w2sv.autocrop.activities.cropexamination.fragments.manualcrop.ManualCropFragment
import com.w2sv.autocrop.activities.cropexamination.fragments.saveall.SaveAllFragment
import com.w2sv.autocrop.activities.ApplicationFragment
import com.w2sv.autocrop.cropping.CropEdges
import com.w2sv.autocrop.cropping.cropbundle.Crop
import com.w2sv.autocrop.databinding.FragmentCroppagerBinding
import com.w2sv.autocrop.preferences.BooleanPreferences
import com.w2sv.autocrop.ui.animate
import com.w2sv.autocrop.ui.crossFade
import com.w2sv.autocrop.utils.extensions.loadBitmap
import com.w2sv.autocrop.utils.extensions.snackyBuilder
import com.w2sv.kotlinutils.extensions.numericallyInflected
import dagger.hilt.android.AndroidEntryPoint
import de.mateware.snacky.Snacky
import javax.inject.Inject

@AndroidEntryPoint
class CropPagerFragment :
    ApplicationFragment<FragmentCroppagerBinding>(FragmentCroppagerBinding::class.java),
    CropDialog.ResultListener,
    CropEntiretyDialog.ResultListener,
    ManualCropFragment.ResultListener,
    CropPagerInstructionsDialog.OnDismissedListener {

    @Inject
    lateinit var booleanPreferences: BooleanPreferences

    private val viewModel by viewModels<CropPagerViewModel>()
    private val activityViewModel by activityViewModels<CropExaminationActivityViewModel>()

    override fun onAttach(context: Context) {
        super.onAttach(context)

        sharedElementReturnTransition = TransitionInflater.from(context)
            .inflateTransition(android.R.transition.move)
    }

    private lateinit var viewPagerProxy: CropPager

    fun onBackPress() {
        viewModel.backPressHandler(
            {
                requireActivity()
                    .snackyBuilder("Tap again to return to main screen")
                    .setSnackbarRepelledView()
                    .build()
                    .show()
            },
            {
                castActivity<CropExaminationActivity>().startMainActivity()
            }
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewPagerProxy = CropPager(
            binding.viewPager,
            viewModel.dataSet
        )
        viewModel.setLiveDataObservers()
    }

    private fun CropPagerViewModel.setLiveDataObservers() {
        dataSet.livePosition.observe(viewLifecycleOwner) { position ->
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
                        binding.snackbarRepelledLayout
                    )
                }
                    ?: binding.snackbarRepelledLayout.show()

                if (!booleanPreferences.cropPagerInstructionsShown)
                    lifecycleScope.launchDelayed(resources.getLong(R.integer.delay_small)) {
                        CropPagerInstructionsDialog()
                            .show(childFragmentManager)
                        booleanPreferences.cropPagerInstructionsShown = true
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
        with(activityViewModel) {
            if (!showedDismissedScreenshotsSnackbar && nDismissedScreenshots != 0) {
                requireActivity().snackyBuilder(
                    SpannableStringBuilder()
                        .append("Couldn't find crop bounds for")
                        .bold {
                            color(
                                requireContext().getThemedColor(R.color.highlight)
                            ) { append(" $nDismissedScreenshots") }
                        }
                        .append(" image".numericallyInflected(nDismissedScreenshots))
                )
                    .setSnackbarRepelledView()
                    .setIcon(com.w2sv.permissionhandler.R.drawable.ic_error_24)
                    .build()
                    .show()
            }
        }
    }

    override fun onResult(cropEdges: CropEdges) {
        processAdjustedCropEdges(cropEdges)
        lifecycleScope.launchDelayed(resources.getLong(R.integer.delay_small)) {
            requireActivity().snackyBuilder(
                "Adjusted crop"
            )
                .setSnackbarRepelledView()
                .setIcon(requireContext().getColoredIcon(R.drawable.ic_check_24, R.color.success))
                .build()
                .show()
        }
    }

    /**
     * Set new [Crop] in [viewModel].dataSet.currentPosition and notify [CropPager.Adapter]
     */
    private fun processAdjustedCropEdges(adjustedEdges: CropEdges) {
        with(viewModel.dataSet.liveElement) {
            crop = Crop.fromScreenshot(
                requireContext().contentResolver.loadBitmap(screenshot.uri),
                screenshot.mediaStoreData.diskUsage,
                adjustedEdges
            )
        }

        (binding.viewPager.adapter!! as CropPager.Adapter).notifyItemChanged(
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
            with(activityViewModel) {
                singularCropSavingJob = lifecycleScope.launch(
                    makeCropBundleProcessor(
                        dataSetPosition,
                        booleanPreferences.deleteScreenshots,
                        requireContext()
                    )
                )
            }

        if (viewModel.dataSet.size == 1)
            castActivity<CropExaminationActivity>().replaceWithSubsequentFragment()
        else
            viewPagerProxy.removeView(dataSetPosition)
    }

    override fun onResult(confirmed: Boolean) {
        if (confirmed)
            getFragmentHostingActivity()
                .fragmentReplacementTransaction(SaveAllFragment(), true)
                .commit()
        else
            castActivity<CropExaminationActivity>().replaceWithSubsequentFragment()
    }

    private fun Snacky.Builder.setSnackbarRepelledView(): Snacky.Builder =
        setView(binding.snackbarRepelledHostingLayout)

    /**
     * Cancel and nullify scroller if set, i.e. running
     */
    override fun onPause() {
        super.onPause()

        with(viewModel) {
            scroller?.cancel()
            scroller = null
        }
    }
}