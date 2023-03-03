package com.w2sv.autocrop.activities.examination.fragments.pager

import android.content.Context
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.view.View
import android.widget.PopupMenu
import android.widget.Toast
import androidx.core.text.bold
import androidx.core.text.color
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import androidx.viewpager2.widget.ViewPager2
import com.daimajia.androidanimations.library.Techniques
import com.w2sv.androidutils.BackPressHandler
import com.w2sv.androidutils.extensions.getHtmlText
import com.w2sv.androidutils.extensions.getLong
import com.w2sv.androidutils.extensions.hide
import com.w2sv.androidutils.extensions.makeToast
import com.w2sv.androidutils.extensions.postValue
import com.w2sv.androidutils.extensions.remove
import com.w2sv.androidutils.extensions.show
import com.w2sv.androidutils.extensions.showToast
import com.w2sv.androidutils.extensions.viewModel
import com.w2sv.autocrop.R
import com.w2sv.autocrop.activities.AppFragment
import com.w2sv.autocrop.activities.crop.domain.CropResults
import com.w2sv.autocrop.activities.examination.ExaminationActivity
import com.w2sv.autocrop.activities.examination.fragments.adjustment.CropAdjustmentFragment
import com.w2sv.autocrop.activities.examination.fragments.comparison.ComparisonFragment
import com.w2sv.autocrop.activities.examination.fragments.pager.dialogs.AbstractCropSavingDialogFragment
import com.w2sv.autocrop.activities.examination.fragments.pager.dialogs.CropSavingDialogFragment
import com.w2sv.autocrop.activities.examination.fragments.pager.dialogs.RecropDialogFragment
import com.w2sv.autocrop.activities.examination.fragments.pager.dialogs.SaveAllCropsDialogFragment
import com.w2sv.autocrop.activities.examination.fragments.pager.model.CropProcedure
import com.w2sv.autocrop.activities.examination.fragments.saveall.SaveAllFragment
import com.w2sv.autocrop.databinding.CropPagerBinding
import com.w2sv.autocrop.ui.model.Click
import com.w2sv.autocrop.ui.views.KEEP_MENU_ITEM_OPEN_ON_CLICK
import com.w2sv.autocrop.ui.views.VisualizationType
import com.w2sv.autocrop.ui.views.animate
import com.w2sv.autocrop.ui.views.currentViewHolder
import com.w2sv.autocrop.ui.views.makeOnClickPersistent
import com.w2sv.autocrop.ui.views.notifyCurrentItemChanged
import com.w2sv.autocrop.ui.views.toggleCheck
import com.w2sv.autocrop.ui.views.visualize
import com.w2sv.autocrop.utils.extensions.holdingSingularElement
import com.w2sv.autocrop.utils.getFragment
import com.w2sv.bidirectionalviewpager.recyclerview.ImageViewHolder
import com.w2sv.cropbundle.Crop
import com.w2sv.cropbundle.CropBundle
import com.w2sv.cropbundle.cropping.CropEdges
import com.w2sv.cropbundle.cropping.crop
import com.w2sv.kotlinutils.extensions.numericallyInflected
import com.w2sv.preferences.BooleanPreferences
import com.w2sv.preferences.IntPreferences
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
    CropAdjustmentFragment.ResultListener,
    RecropDialogFragment.Listener {

    companion object {
        fun getInstance(cropResults: CropResults): CropPagerFragment =
            getFragment(CropPagerFragment::class.java, CropResults.EXTRA to cropResults)
    }

    @HiltViewModel
    class ViewModel @Inject constructor(
        savedStateHandle: SavedStateHandle,
        private val intPreferences: IntPreferences,
        val booleanPreferences: BooleanPreferences,
        @ApplicationContext context: Context
    ) : androidx.lifecycle.ViewModel() {

        val dataSet = CropPager.DataSet(ExaminationActivity.ViewModel.cropBundles)

        /**
         * CropSavingDialogs
         */

        fun getCropSavingDialogOnClick(click: Click): AbstractCropSavingDialogFragment? =
            when {
                doAutoScrollLive.value == true -> null
                click == Click.Single || dataSet.holdingSingularElement -> getSaveCropDialog(true)
                else -> getSaveAllCropsDialog(true)
            }

        fun getSaveCropDialog(showDismissButton: Boolean): CropSavingDialogFragment =
            CropSavingDialogFragment.getInstance(dataSet.livePosition.value!!, showDismissButton)

        fun getSaveAllCropsDialog(showDismissButton: Boolean): SaveAllCropsDialogFragment =
            SaveAllCropsDialogFragment.getInstance(dataSet.size, showDismissButton)

        /**
         * AutoScroll
         */

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

        val doAutoScrollLive: LiveData<Boolean> = MutableLiveData(booleanPreferences.autoScroll && dataSet.size > 1)

        private fun maxAutoScrolls(): Int =
            dataSet.size - dataSet.livePosition.value!!

        /**
         * Crop Results Notification
         */

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
                            color(context.getColor(R.color.highlight)) {
                                append(" ${cropResults.nNotCroppableImages}")
                            }
                        }
                        append(" ${"screenshot".numericallyInflected(cropResults.nNotCroppableImages)}")
                    }
                    ifEmpty { null }
                }

        private var showedCropResultsNotification: Boolean = false

        /**
         * Other
         */

        fun getRecropDialog(): RecropDialogFragment =
            RecropDialogFragment.getInstance(
                dataSet.livePosition.value!!,
                dataSet.liveElement.adjustedEdgeThreshold
                    ?: intPreferences.edgeCandidateThreshold
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
            if (it.holdingSingularElement && lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED))
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
                if (!viewModel.dataSet.holdingSingularElement)
                    add(allCropsButtonsWLabel)
            }
                .visualize(
                    if (cancelledScrolling) VisualizationType.FadeIn else VisualizationType.Instant
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
            requireViewBoundFragmentActivity().fragmentReplacementTransaction(
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
            requireViewBoundFragmentActivity().fragmentReplacementTransaction(
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
                                isChecked = viewModel.booleanPreferences.autoScroll
                                makeOnClickPersistent(requireContext())
                            }
                        setOnMenuItemClickListener { item ->
                            when (item.itemId) {
                                R.id.crop_pager_item_auto_scroll -> {
                                    item.toggleCheck { newValue ->
                                        viewModel.booleanPreferences.autoScroll = newValue
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

    override fun onApplyAdjustedCropEdges(cropEdges: CropEdges) {
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
            requireContext().showToast("Adjusted crop")
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
        if (viewModel.dataSet.holdingSingularElement) {
            return requireCastActivity<ExaminationActivity>().invokeExitFragment()
        }

        cropPager.scrollToNextViewAndRemoveCurrent(dataSetPosition)

        viewModel.lastCropProcedureToast?.cancel()
        viewModel.lastCropProcedureToast = requireContext()
            .makeToast(
                when (cropProcedure) {
                    CropProcedure.Discard -> "Discarded crop"
                    CropProcedure.Save -> "Saved crop"
                }
            )
            .also {
                it.show()
            }
    }

    override fun onSaveAllCrops() {
        requireViewBoundFragmentActivity()
            .fragmentReplacementTransaction(
                SaveAllFragment.getInstance(ArrayList(viewModel.dataSet.indices.toList())),
                true
            )
            .commit()
    }

    override fun onDiscardAllCrops() {
        requireCastActivity<ExaminationActivity>().invokeExitFragment()
    }

    override fun onDoRecrop(cropBundlePosition: Int, threshold: Double) {
        cropPager.pager.isEnabled = false
        binding.recropProgressBar.show()

        requireContext().showToast(
            when (viewModel.dataSet[cropBundlePosition].recropAndUpdate(threshold)) {
                false -> "No Crop Edges found for adjusted Settings"
                true -> {
                    cropPager.pager.notifyCurrentItemChanged()
                    "Updated Crop"
                }
            }
        )

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
                requireContext().showToast("Tap again to return to main screen")
            },
            {
                requireCastActivity<ExaminationActivity>().startMainActivity()
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