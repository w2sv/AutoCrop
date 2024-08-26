package com.w2sv.autocrop.ui.screen.pager

import android.os.Bundle
import android.view.View
import android.widget.PopupMenu
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.coroutineScope
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.viewpager2.widget.ViewPager2
import com.daimajia.androidanimations.library.Techniques
import com.w2sv.androidutils.res.getText
import com.w2sv.androidutils.view.hide
import com.w2sv.androidutils.view.remove
import com.w2sv.androidutils.view.show
import com.w2sv.androidutils.widget.showToast
import com.w2sv.autocrop.AppFragment
import com.w2sv.autocrop.R
import com.w2sv.autocrop.databinding.CropPagerBinding
import com.w2sv.autocrop.ui.screen.CropBundleViewModel
import com.w2sv.autocrop.ui.screen.cropNavGraphViewModel
import com.w2sv.autocrop.ui.screen.pager.dialog.cropsaving.CropProcedureDialogFragment
import com.w2sv.autocrop.ui.screen.pager.dialog.cropsaving.CropsProcedureDialogFragment
import com.w2sv.autocrop.ui.screen.pager.dialog.recrop.RecropDialogFragment
import com.w2sv.autocrop.ui.screen.pager.model.CropProcedure
import com.w2sv.autocrop.ui.screen.pager.view.CropPagerWrapper
import com.w2sv.autocrop.ui.util.nonNullValue
import com.w2sv.autocrop.ui.views.KEEP_MENU_ITEM_OPEN_ON_CLICK
import com.w2sv.autocrop.ui.views.VisualizationMethod
import com.w2sv.autocrop.ui.views.animate
import com.w2sv.autocrop.ui.views.currentViewHolder
import com.w2sv.autocrop.ui.views.makeOnClickPersistent
import com.w2sv.autocrop.ui.views.notifyCurrentItemChanged
import com.w2sv.autocrop.ui.views.toggleCheck
import com.w2sv.autocrop.ui.views.visualize
import com.w2sv.autocrop.util.extensions.containsSingularElement
import com.w2sv.autocrop.util.extensions.launchAfterShortDelay
import com.w2sv.bidirectionalviewpager.recyclerview.ImageViewHolder
import com.w2sv.cropbundle.Crop
import com.w2sv.cropbundle.CropBundle
import com.w2sv.cropbundle.cropping.CropSensitivity
import com.w2sv.cropbundle.cropping.crop
import com.w2sv.cropbundle.cropping.model.CropEdges
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.withCreationCallback
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val AUTO_SCROLL_PERIOD = 1000L

@AndroidEntryPoint
class CropPagerScreenFragment :
    AppFragment<CropPagerBinding>(CropPagerBinding::class.java),
    CropProcedureDialogFragment.ResultListener,
    CropsProcedureDialogFragment.ResultListener,
    RecropDialogFragment.Listener {

    private val viewModel by viewModels<CropPagerScreenViewModel>(
        extrasProducer = {
            defaultViewModelCreationExtras
                .withCreationCallback<CropPagerScreenViewModel.AssistedFactory> { factory ->
                    factory.create(cropBundleVM.cropBundles)
                }
        }
    )
    private val cropBundleVM by cropNavGraphViewModel<CropBundleViewModel>()

    private lateinit var cropPagerWrapper: CropPagerWrapper

    override val onBackPressed: () -> Unit
        get() = {
            viewModel.backPressHandler(
                onFirstPress = {
                    requireContext().showToast(getString(R.string.tap_again_to_return_to_main_screen))
                },
                onSecondPress = {
                    navigateToExitFragment()
                }
            )
        }

    //    override fun onCreate(savedInstanceState: Bundle?) {
    //        super.onCreate(savedInstanceState)
    //
    //        setFragmentResultListener(CropAdjustmentFragment.REQUEST_KEY) { _, bundle ->
    //            applyAdjustedCropEdges(bundle.getParcelableCompat(CropEdges.EXTRA)!!)
    //        }
    //    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        cropPagerWrapper = CropPagerWrapper(
            pager = binding.viewPager,
            dataSet = viewModel.dataSet,
            onClickListener = {
                showCropProcedureDialog()
            },
            onLongClickListener = {
                if (viewModel.dataSet.containsSingularElement) {
                    false
                }
                else {
                    showCropsProcedureDialog()
                    true
                }
            }
        )

        viewModel.setLiveDataObservers()
        binding.setOnClickListeners()
    }

    private fun CropPagerScreenViewModel.setLiveDataObservers() {
        dataSet.livePosition.observe(viewLifecycleOwner) {
            binding.onDataSetPositionChanged(it)
        }

        autoScrolling.observe(viewLifecycleOwner) {
            binding.onDoAutoScrollChanged(it)
        }

        dataSet.observe(viewLifecycleOwner) {
            if (it.containsSingularElement && lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                binding.allCropsButtonsWLabel.animate(Techniques.ZoomOut)
            }
        }
    }

    private fun CropPagerBinding.onDataSetPositionChanged(position: Int) {
        with(viewModel.dataSet[position].crop) {
            discardingStatisticsTv.text = resources.getText(
                R.string.discarding_statistics,
                "$discardedPercentage%",
                discardedFileSizeFormatted
            )
        }

        viewModel.dataSet.pageIndex(position).let { pageIndex ->
            pageIndicationTv.updateText(pageIndex + 1, viewModel.dataSet.size)
        }
    }

    private var autoScrollJob: Job? = null

    private fun CropPagerBinding.onDoAutoScrollChanged(doAutoScroll: Boolean) {
        // en-/disable viewPager input
        viewPager.isUserInputEnabled = !doAutoScroll

        if (doAutoScroll) {
            cancelAutoScrollButton.show()
            lifecycle.coroutineScope.launch {
                if (autoScrollJob == null)
                    delay(AUTO_SCROLL_PERIOD)

                autoScrollJob = viewPager.scrollPeriodically(
                    this,
                    viewModel.autoScrollCount(),
                    AUTO_SCROLL_PERIOD
                ) {
                    viewModel.cancelAutoScroll()
                }
            }
        }
        else {
            val cancelledScrolling = autoScrollJob?.let {
                it.cancel()
                true
            }
                ?: false

            buildList {
                add(currentCropLayout)
                add(popupMenuButton)
                if (!viewModel.dataSet.containsSingularElement) {
                    add(allCropsButtonsWLabel)
                }
            }
                .visualize(
                    if (cancelledScrolling) VisualizationMethod.FadeIn else VisualizationMethod.Instantaneous
                )

            launchAfterShortDelay {
                viewModel.showCropResultsToastIfApplicable(requireContext())
            }

            viewPager.setPageTransformer(CubeOutPageTransformer())
            cancelAutoScrollButton.hide()
        }
    }

    private fun CropPagerBinding.setOnClickListeners() {
        discardAllButton.setOnClickListener {
            navigateToExitFragment()
        }
        saveAllButton.setOnClickListener {
            showCropsProcedureDialog()
        }
        cancelAutoScrollButton.setOnClickListener {
            viewModel.cancelAutoScroll()
        }
        discardCropButton.setOnClickListener {
            removeView(viewModel.dataSet.livePosition.value!!, CropProcedure.Discard)
        }
        saveCropButton.setOnClickListener {
            showCropProcedureDialog()
        }
        manualCropButton.setOnClickListener {
            navController.navigate(CropPagerScreenFragmentDirections.navigateToCropAdjustmentScreen(viewModel.dataSet.liveElement))
        }
        recropButton.setOnClickListener {
            navController.navigate(CropPagerScreenFragmentDirections.showRecropDialog(viewModel.dataSet.liveElement.cropSensitivity))
        }
        comparisonButton.setOnClickListener {
            val cropImageView =
                binding
                    .viewPager
                    .currentViewHolder<ImageViewHolder>()!!
                    .imageView

            navController.navigate(
                CropPagerScreenFragmentDirections.navigateToComparisonScreen(viewModel.dataSet.liveElement),
                FragmentNavigatorExtras(cropImageView to cropImageView.transitionName)
            )
        }
        popupMenuButton.setOnClickListener {
            with(PopupMenu(requireContext(), it)) {
                menuInflater.inflate(
                    R.menu.crop_pager,
                    menu
                )
                menu
                    .apply {
                        findItem(R.id.crop_pager_item_auto_scroll)
                            .apply {
                                isCheckable = true
                                isChecked = viewModel.doAutoScroll.value
                                makeOnClickPersistent(requireContext())
                            }
                        setOnMenuItemClickListener { item ->
                            when (item.itemId) {
                                R.id.crop_pager_item_auto_scroll -> {
                                    item.toggleCheck { newValue ->
                                        viewModel.saveDoAutoScroll(newValue)
                                    }

                                    KEEP_MENU_ITEM_OPEN_ON_CLICK
                                }

                                else -> true
                            }
                        }
                    }
                show()
            }
        }
    }

    private fun showCropProcedureDialog() {
        navController.navigate(CropPagerScreenFragmentDirections.showCropProcedureDialog(viewModel.dataSet.livePosition.nonNullValue))
    }

    private fun showCropsProcedureDialog() {
        navController.navigate(CropPagerScreenFragmentDirections.showCropsProcedureDialog())
    }

    private fun navigateToExitFragment() {
        navController.navigate(CropPagerScreenFragmentDirections.navigateToExitScreen())
    }

    private fun applyAdjustedCropEdges(cropEdges: CropEdges) {
        viewModel.dataSet.liveElement.let {
            it.crop = Crop.fromScreenshot(
                screenshotBitmap = it.screenshot.getBitmap(requireContext().contentResolver),
                screenshotDiskUsage = it.screenshot.mediaStoreData.diskUsage,
                edges = cropEdges
            )
        }

        binding.viewPager.adapter!!.notifyItemChanged(
            binding.viewPager.currentItem,
            viewModel.dataSet.size
        )

        launchAfterShortDelay {
            requireContext().showToast(getString(R.string.adjusted_crop))
        }
    }

    /**
     * Increment nSavedCrops if applicable
     *
     * triggers activity exit if [viewModel].dataSet about to be exhausted OR
     * hide pageIndicationSeekBar AND/OR
     * removes view, procedure action has been selected for, from pager
     */
    override fun onSaveCrop(dataSetPosition: Int) {
        cropBundleVM.processCropBundle(
            dataSetPosition,
            requireContext()
        )
        removeView(dataSetPosition, CropProcedure.Save)
    }

    override fun onDiscardCrop(dataSetPosition: Int) {
        removeView(dataSetPosition, CropProcedure.Discard)
    }

    private fun removeView(dataSetPosition: Int, cropProcedure: CropProcedure) {
        if (viewModel.dataSet.containsSingularElement) {
            return navigateToExitFragment()
        }

        cropPagerWrapper.scrollToNextViewAndRemoveCurrent(dataSetPosition)

        cropProcedure.notificationMessageRes?.let {
            viewModel.showCropProcedureResultToast(requireContext(), it)
        }
    }

    override fun onSaveAllCrops() {
        navController.navigate(CropPagerScreenFragmentDirections.navigateToSaveAllScreen())
    }

    override fun onDiscardAllCrops() {
        navigateToExitFragment()
    }

    override fun onRecrop(@CropSensitivity cropSensitivity: Int) =
        onRecropWrapper {
            requireContext().showToast(
                when (viewModel.dataSet.liveElement.recropAndUpdate(cropSensitivity)) {
                    false -> R.string.no_crop_edges_found_for_adjusted_settings
                    true -> {
                        cropPagerWrapper.pager.notifyCurrentItemChanged()
                        R.string.updated_crop
                    }
                }
            )
        }

    //    override fun onRecropAll(threshold: Double) =
    //        onRecropWrapper {
    //            requireContext().showToast(
    //                viewModel.dataSet.map { it.recropAndUpdate(threshold) }.groupingBy { it }.eachCount().let {
    //                    when (it.getOrDefault(false, 0)) {
    //                        viewModel.dataSet.size -> "No Crop Edges found for adjusted Settings"
    //                        else -> "Updated ${it.getValue(true)} crops"
    //                    }
    //                }
    //            )
    //        }

    private inline fun onRecropWrapper(f: () -> Unit) {
        cropPagerWrapper.pager.isEnabled = false
        binding.recropProgressBar.show()

        f()

        binding.recropProgressBar.remove()
        cropPagerWrapper.pager.isEnabled = true
    }

    private fun CropBundle.recropAndUpdate(@CropSensitivity cropSensitivity: Int): Boolean {
        val screenshotBitmap = screenshot.getBitmap(requireContext().contentResolver)
        return screenshotBitmap.crop(cropSensitivity)?.let { (edges, candidates) ->
            crop = Crop.fromScreenshot(
                screenshotBitmap,
                screenshot.mediaStoreData.diskUsage,
                edges
            )
            edgeCandidates = candidates
            this.cropSensitivity = cropSensitivity
            true
        }
            ?: false
    }
}

private class CubeOutPageTransformer : ViewPager2.PageTransformer {
    override fun transformPage(page: View, position: Float) {
        with(page) {
            pivotX = (if (position < 0) width else 0).toFloat()
            pivotY = height * 0.5f
            rotationY = 90f * position
        }
    }
}

private fun ViewPager2.scrollPeriodically(
    coroutineScope: CoroutineScope,
    maxScrolls: Int,
    period: Long,
    onFinishedListener: () -> Unit
): Job =
    coroutineScope.launch(Dispatchers.Main) {
        (0 until maxScrolls).forEach {
            setCurrentItem(currentItem + 1, true)
            if (it != maxScrolls - 1)
                delay(period)
        }
        onFinishedListener()
    }