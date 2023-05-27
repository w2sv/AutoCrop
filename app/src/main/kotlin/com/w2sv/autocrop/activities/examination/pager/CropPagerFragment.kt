package com.w2sv.autocrop.activities.examination.pager

import android.content.Context
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.view.View
import android.widget.PopupMenu
import android.widget.Toast
import androidx.core.text.bold
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import androidx.viewpager2.widget.ViewPager2
import com.daimajia.androidanimations.library.Techniques
import com.w2sv.androidutils.eventhandling.BackPressHandler
import com.w2sv.androidutils.generic.getParcelableCompat
import com.w2sv.androidutils.lifecycle.postValue
import com.w2sv.androidutils.notifying.makeToast
import com.w2sv.androidutils.notifying.showToast
import com.w2sv.androidutils.ui.dialogs.show
import com.w2sv.androidutils.ui.resources.getHtmlText
import com.w2sv.androidutils.ui.resources.getLong
import com.w2sv.androidutils.ui.views.hide
import com.w2sv.androidutils.ui.views.remove
import com.w2sv.androidutils.ui.views.show
import com.w2sv.autocrop.R
import com.w2sv.autocrop.activities.AppFragment
import com.w2sv.autocrop.activities.ViewBoundFragmentActivity
import com.w2sv.autocrop.activities.crop.domain.CropResults
import com.w2sv.autocrop.activities.examination.ExaminationActivity
import com.w2sv.autocrop.activities.examination.adjustment.CropAdjustmentFragment
import com.w2sv.autocrop.activities.examination.comparison.ComparisonFragment
import com.w2sv.autocrop.activities.examination.pager.dialogs.cropsaving.AbstractCropSavingDialogFragment
import com.w2sv.autocrop.activities.examination.pager.dialogs.cropsaving.CropSavingDialogFragment
import com.w2sv.autocrop.activities.examination.pager.dialogs.cropsaving.SaveAllCropsDialogFragment
import com.w2sv.autocrop.activities.examination.pager.dialogs.recrop.RecropDialogFragment
import com.w2sv.autocrop.activities.examination.pager.model.CropProcedure
import com.w2sv.autocrop.activities.examination.saveall.SaveAllFragment
import com.w2sv.autocrop.databinding.CropPagerBinding
import com.w2sv.autocrop.ui.model.Click
import com.w2sv.autocrop.ui.views.KEEP_MENU_ITEM_OPEN_ON_CLICK
import com.w2sv.autocrop.ui.views.VisualizationMethod
import com.w2sv.autocrop.ui.views.animate
import com.w2sv.autocrop.ui.views.currentViewHolder
import com.w2sv.autocrop.ui.views.makeOnClickPersistent
import com.w2sv.autocrop.ui.views.notifyCurrentItemChanged
import com.w2sv.autocrop.ui.views.toggleCheck
import com.w2sv.autocrop.ui.views.visualize
import com.w2sv.autocrop.utils.extensions.isHoldingSingularElement
import com.w2sv.autocrop.utils.extensions.launchAfterShortDelay
import com.w2sv.autocrop.utils.getFragment
import com.w2sv.autocrop.utils.requireCastActivity
import com.w2sv.bidirectionalviewpager.recyclerview.ImageViewHolder
import com.w2sv.common.datastore.Repository
import com.w2sv.cropbundle.Crop
import com.w2sv.cropbundle.CropBundle
import com.w2sv.cropbundle.cropping.CropEdges
import com.w2sv.cropbundle.cropping.crop
import com.w2sv.kotlinutils.extensions.numericallyInflected
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class CropPagerFragment :
    AppFragment<CropPagerBinding>(CropPagerBinding::class.java),
    CropSavingDialogFragment.ResultListener,
    SaveAllCropsDialogFragment.ResultListener,
    RecropDialogFragment.Listener {

    companion object {
        fun getInstance(cropResults: CropResults): CropPagerFragment =
            getFragment(CropPagerFragment::class.java, CropResults.EXTRA to cropResults)
    }

    @HiltViewModel
    class ViewModel @Inject constructor(
        savedStateHandle: SavedStateHandle,
        val repository: Repository,
        @ApplicationContext context: Context
    ) : androidx.lifecycle.ViewModel() {

        val dataSet = CropPager.DataSet(ExaminationActivity.ViewModel.cropBundles)

        // ==================
        // CropSavingDialogs
        // ==================

        fun getCropSavingDialogOnClick(click: Click): AbstractCropSavingDialogFragment? =
            when {
                doAutoScrollLive.value == true -> null
                click == Click.Single || dataSet.isHoldingSingularElement -> getSaveCropDialog(true)
                else -> getSaveAllCropsDialog(true)
            }

        fun getSaveCropDialog(showDismissButton: Boolean): CropSavingDialogFragment =
            CropSavingDialogFragment.getInstance(dataSet.livePosition.value!!, showDismissButton)

        fun getSaveAllCropsDialog(showDismissButton: Boolean): SaveAllCropsDialogFragment =
            SaveAllCropsDialogFragment.getInstance(dataSet.size, showDismissButton)

        // ==================
        // AutoScroll
        // ==================

        fun launchAutoScrollCoroutine(coroutineScope: CoroutineScope, viewPager: ViewPager2, period: Long) {
            coroutineScope.launch {
                if (autoScrollCoroutine == null)
                    delay(period)

                autoScrollCoroutine = viewPager.scrollPeriodically(
                    this,
                    maxAutoScrolls(),
                    period
                ) {
                    doAutoScrollLive.postValue(false)
                }
            }
        }

        var autoScrollCoroutine: Job? = null

        val doAutoScrollLive: LiveData<Boolean> =
            MutableLiveData(repository.autoScroll.value && dataSet.size > 1)

        private fun maxAutoScrolls(): Int =
            dataSet.size - dataSet.livePosition.value!!

        // ==================
        // Crop Results Notification
        // ==================

        fun showCropResultsToastIfApplicable(context: Context) {
            if (uncroppedScreenshotsSnackbarText != null && !showedCropResultsNotification) {
                context.showToast(uncroppedScreenshotsSnackbarText, duration = Toast.LENGTH_LONG)
                showedCropResultsNotification = true
            }
        }

        private val uncroppedScreenshotsSnackbarText: SpannableStringBuilder? =
            SpannableStringBuilder()
                .run {
                    val cropResults = savedStateHandle.get<CropResults>(CropResults.EXTRA)!!

                    if (cropResults.nNotCroppableImages != 0) {
                        append("Couldn't find crop bounds for")
                        bold {
                            append(" ${cropResults.nNotCroppableImages}")
                        }
                        append(" ${"screenshot".numericallyInflected(cropResults.nNotCroppableImages)}")
                    }
                    ifEmpty { null }
                }

        private var showedCropResultsNotification: Boolean = false

        // ==========
        // Other
        // ==========

        fun getRecropDialog(): RecropDialogFragment =
            RecropDialogFragment.getInstance(
                dataSet.livePosition.value!!,
                dataSet.liveElement.adjustedEdgeThreshold
                    ?: repository.edgeCandidateThreshold.value
            )

        val backPressHandler = BackPressHandler(
            viewModelScope,
            context.resources.getLong(R.integer.duration_backpress_confirmation_window)
        )

        var lastCropProcedureToast: Toast? = null
    }

    private val viewModel by viewModels<ViewModel>()

    private val activityViewModel by activityViewModels<ExaminationActivity.ViewModel>()

    private lateinit var cropPager: CropPager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setFragmentResultListener(CropAdjustmentFragment.REQUEST_KEY) { _, bundle ->
            applyAdjustedCropEdges(bundle.getParcelableCompat(CropEdges.EXTRA)!!)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        cropPager = CropPager(
            binding.viewPager,
            viewModel.dataSet,
            onClickListener = {
                viewModel.getCropSavingDialogOnClick(Click.Single)
                    ?.show(childFragmentManager)
            },
            onLongClickListener = {
                viewModel.getCropSavingDialogOnClick(Click.Long)?.let {
                    it.show(childFragmentManager)
                    true
                }
                    ?: false
            }
        )

        viewModel.setLiveDataObservers()
        binding.setOnClickListeners()
    }

    private fun ViewModel.setLiveDataObservers() {
        dataSet.livePosition.observe(viewLifecycleOwner) {
            binding.updateOnDataSetPositionChanged(it)
        }

        doAutoScrollLive.observe(viewLifecycleOwner) {
            binding.updateOnAutoScrollStatusChanged(it)
        }

        dataSet.observe(viewLifecycleOwner) {
            if (it.isHoldingSingularElement && lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED))
                binding.allCropsButtonsWLabel.animate(Techniques.ZoomOut)
        }
    }

    private fun CropPagerBinding.updateOnDataSetPositionChanged(position: Int) {
        with(viewModel.dataSet[position].crop) {
            discardingStatisticsTv.text = resources.getHtmlText(
                R.string.discarding_statistics,
                "$discardedPercentage%",
                discardedFileSizeFormatted
            )
        }

        viewModel.dataSet.pageIndex(position).let { pageIndex ->
            pageIndicationTv.updateText(pageIndex + 1, viewModel.dataSet.size)
        }
    }

    private fun CropPagerBinding.updateOnAutoScrollStatusChanged(doAutoScroll: Boolean) {
        // en-/disable viewPager input
        viewPager.isUserInputEnabled = !doAutoScroll

        if (doAutoScroll) {
            cancelAutoScrollButton.show()
            viewModel.launchAutoScrollCoroutine(
                lifecycleScope,
                viewPager,
                resources.getLong(R.integer.period_auto_scroll)
            )
        }
        else {
            val cancelledScrolling = viewModel.autoScrollCoroutine?.let {
                it.cancel()
                true
            }
                ?: false

            buildList {
                add(currentCropLayout)
                add(popupMenuButton)
                if (!viewModel.dataSet.isHoldingSingularElement) {
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
            requireCastActivity<ExaminationActivity>().invokeExitFragment()
        }
        saveAllButton.setOnClickListener {
            viewModel.getSaveAllCropsDialog(false)
                .show(childFragmentManager)
        }
        //        recropAllButton.setOnClickListener {
        //            RecropAllDialogFragment().show(childFragmentManager)
        //        }
        cancelAutoScrollButton.setOnClickListener {
            viewModel.doAutoScrollLive.postValue(false)
        }
        discardCropButton.setOnClickListener {
            removeView(viewModel.dataSet.livePosition.value!!, CropProcedure.Discard)
        }
        saveCropButton.setOnClickListener {
            viewModel.getSaveCropDialog(false)
                .show(childFragmentManager)
        }
        manualCropButton.setOnClickListener {
            requireCastActivity<ViewBoundFragmentActivity>().fragmentReplacementTransaction(
                CropAdjustmentFragment.getInstance(
                    viewModel.dataSet.livePosition.value!!
                ),
                true
            )
                .addToBackStack(null)
                .commit()
        }
        recropButton.setOnClickListener {
            viewModel.getRecropDialog()
                .show(childFragmentManager)
        }
        comparisonButton.setOnClickListener {
            requireCastActivity<ViewBoundFragmentActivity>().fragmentReplacementTransaction(
                ComparisonFragment.getInstance(viewModel.dataSet.livePosition.value!!)
            )
                .addToBackStack(null)
                .apply {
                    val cropImageView =
                        binding
                            .viewPager
                            .currentViewHolder<ImageViewHolder>()!!
                            .imageView

                    addSharedElement(
                        cropImageView,
                        cropImageView.transitionName
                    )
                }
                .commit()
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
                                isChecked = viewModel.repository.autoScroll.value
                                makeOnClickPersistent(requireContext())
                            }
                        setOnMenuItemClickListener { item ->
                            when (item.itemId) {
                                R.id.crop_pager_item_auto_scroll -> {
                                    item.toggleCheck { newValue ->
                                        viewModel.repository.autoScroll.value = newValue
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

    private fun applyAdjustedCropEdges(cropEdges: CropEdges) {
        viewModel.dataSet.liveElement.let {
            it.crop = Crop.fromScreenshot(
                it.screenshot.getBitmap(requireContext().contentResolver),
                it.screenshot.mediaStoreData.diskUsage,
                cropEdges
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
        activityViewModel.processCropBundleAsScopedCoroutine(
            dataSetPosition,
            requireContext().applicationContext
        )
        removeView(dataSetPosition, CropProcedure.Save)
    }

    override fun onDiscardCrop(dataSetPosition: Int) {
        removeView(dataSetPosition, CropProcedure.Discard)
    }

    private fun removeView(dataSetPosition: Int, cropProcedure: CropProcedure) {
        if (viewModel.dataSet.isHoldingSingularElement) {
            return requireCastActivity<ExaminationActivity>().invokeExitFragment()
        }

        cropPager.scrollToNextViewAndRemoveCurrent(dataSetPosition)

        viewModel.lastCropProcedureToast?.cancel()
        viewModel.lastCropProcedureToast = requireContext()
            .makeToast(cropProcedure.notificationMessageRes)
            .also {
                it.show()
            }
    }

    override fun onSaveAllCrops() {
        requireCastActivity<ViewBoundFragmentActivity>()
            .fragmentReplacementTransaction(
                SaveAllFragment.getInstance(ArrayList(viewModel.dataSet.indices.toList())),
                true
            )
            .commit()
    }

    override fun onDiscardAllCrops() {
        requireCastActivity<ExaminationActivity>().invokeExitFragment()
    }

    override fun onRecrop(cropBundlePosition: Int, threshold: Double) =
        onRecropWrapper {
            requireContext().showToast(
                when (viewModel.dataSet[cropBundlePosition].recropAndUpdate(threshold)) {
                    false -> R.string.no_crop_edges_found_for_adjusted_settings
                    true -> {
                        cropPager.pager.notifyCurrentItemChanged()
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
        cropPager.pager.isEnabled = false
        binding.recropProgressBar.show()

        f()

        binding.recropProgressBar.remove()
        cropPager.pager.isEnabled = true
    }

    private fun CropBundle.recropAndUpdate(threshold: Double): Boolean {
        val screenshotBitmap = screenshot.getBitmap(requireContext().contentResolver)
        return screenshotBitmap.crop(threshold)?.let { (edges, candidates) ->
            crop = Crop.fromScreenshot(
                screenshotBitmap,
                screenshot.mediaStoreData.diskUsage,
                edges
            )
            edgeCandidates = candidates
            adjustedEdgeThreshold = threshold.toInt()
            true
        }
            ?: false
    }

    fun onBackPress() {
        viewModel.backPressHandler(
            {
                requireContext().showToast(getString(R.string.tap_again_to_return_to_main_screen))
            },
            {
                activityViewModel.startMainActivity(requireActivity())
            }
        )
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

private class CubeOutPageTransformer : ViewPager2.PageTransformer {
    override fun transformPage(page: View, position: Float) {
        with(page) {
            pivotX = (if (position < 0) width else 0).toFloat()
            pivotY = height * 0.5f
            rotationY = 90f * position
        }
    }
}