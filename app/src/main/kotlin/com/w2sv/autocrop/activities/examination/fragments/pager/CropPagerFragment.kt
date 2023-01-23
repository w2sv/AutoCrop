package com.w2sv.autocrop.activities.examination.fragments.pager

import android.content.Context
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.view.View
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
import com.w2sv.androidutils.BackPressListener
import com.w2sv.androidutils.extensions.getHtmlText
import com.w2sv.androidutils.extensions.getLong
import com.w2sv.androidutils.extensions.hide
import com.w2sv.androidutils.extensions.hideSystemBars
import com.w2sv.androidutils.extensions.postValue
import com.w2sv.androidutils.extensions.show
import com.w2sv.androidutils.extensions.viewModel
import com.w2sv.autocrop.R
import com.w2sv.autocrop.activities.AppFragment
import com.w2sv.autocrop.activities.crop.CropResults
import com.w2sv.autocrop.activities.examination.ExaminationActivity
import com.w2sv.autocrop.activities.examination.fragments.adjustment.CropAdjustmentFragment
import com.w2sv.autocrop.activities.examination.fragments.comparison.ComparisonFragment
import com.w2sv.autocrop.activities.examination.fragments.pager.dialogs.CropSavingDialog
import com.w2sv.autocrop.activities.examination.fragments.pager.dialogs.SaveAllCropsDialog
import com.w2sv.autocrop.activities.examination.fragments.pager.dialogs.SaveCropDialog
import com.w2sv.autocrop.activities.examination.fragments.saveall.SaveAllFragment
import com.w2sv.autocrop.activities.getFragment
import com.w2sv.autocrop.cropbundle.Crop
import com.w2sv.autocrop.cropbundle.cropping.CropEdges
import com.w2sv.autocrop.cropbundle.io.extensions.loadBitmap
import com.w2sv.autocrop.databinding.FragmentCroppagerBinding
import com.w2sv.autocrop.preferences.BooleanPreferences
import com.w2sv.autocrop.preferences.GlobalFlags
import com.w2sv.autocrop.ui.Click
import com.w2sv.autocrop.ui.CubeOutPageTransformer
import com.w2sv.autocrop.ui.animate
import com.w2sv.autocrop.ui.currentViewHolder
import com.w2sv.autocrop.ui.fadeIn
import com.w2sv.autocrop.ui.scrollPeriodically
import com.w2sv.autocrop.utils.extensions.onHalfwayShown
import com.w2sv.autocrop.utils.extensions.showToast
import com.w2sv.bidirectionalviewpager.recyclerview.ImageViewHolder
import com.w2sv.kotlinutils.extensions.numericallyInflected
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import de.mateware.snacky.Snacky
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import slimber.log.i
import javax.inject.Inject

@AndroidEntryPoint
class CropPagerFragment :
    AppFragment<FragmentCroppagerBinding>(FragmentCroppagerBinding::class.java),
    SaveCropDialog.ResultListener,
    SaveAllCropsDialog.ResultListener,
    CropAdjustmentFragment.ResultListener {

    companion object {
        fun getInstance(cropResults: CropResults): CropPagerFragment =
            getFragment(CropPagerFragment::class.java, CropResults.EXTRA to cropResults)
    }

    @HiltViewModel
    class ViewModel @Inject constructor(
        savedStateHandle: SavedStateHandle,
        booleanPreferences: BooleanPreferences,
        @ApplicationContext context: Context
    ) : androidx.lifecycle.ViewModel() {

        val dataSet = CropPager.DataSet(ExaminationActivity.ViewModel.cropBundles)

        val singleCropRemaining: Boolean
            get() = dataSet.size == 1

        /**
         * CropSavingDialogs
         */

        fun getCropSavingDialogOnClick(click: Click): CropSavingDialog? =
            when {
                doAutoScrollLive.value == true -> null
                click == Click.Single || singleCropRemaining -> getSaveCropDialog(true)
                else -> getSaveAllCropsDialog(true)
            }

        fun getSaveCropDialog(showDismissButton: Boolean): SaveCropDialog =
            SaveCropDialog.getInstance(dataSet.livePosition.value!!, showDismissButton)

        fun getSaveAllCropsDialog(showDismissButton: Boolean): SaveAllCropsDialog =
            SaveAllCropsDialog.getInstance(dataSet.size, showDismissButton)

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
         * Crop Results Snackbar
         */

        fun showCropResultsSnackbarIfApplicable(
            coroutineScope: CoroutineScope,
            getSnackyBuilder: (CharSequence) -> Snacky.Builder
        ) {
            if (uncroppedScreenshotsSnackbarText != null && !showedCropResultsSnackbar)
                getSnackyBuilder(uncroppedScreenshotsSnackbarText)
                    .setIcon(com.w2sv.permissionhandler.R.drawable.ic_error_24)
                    .build()
                    .onHalfwayShown(coroutineScope) {
                        showedCropResultsSnackbar = true
                    }
                    .show()
        }

        private val uncroppedScreenshotsSnackbarText: SpannableStringBuilder? =
            SpannableStringBuilder()
                .run {
                    val cropResults = savedStateHandle.get<CropResults>(CropResults.EXTRA)!!
                    i { "$cropResults" }

                    if (cropResults.nNotCroppableImages != 0) {
                        append("Couldn't find crop bounds for")
                        bold {
                            color(context.getColor(R.color.highlight)) {
                                append(" ${cropResults.nNotCroppableImages}")
                            }
                        }
                        append(" ${"screenshot".numericallyInflected(cropResults.nNotCroppableImages)}")
                    }

                    if (cropResults.nNotOpenableImages != 0) {
                        append(
                            if (isEmpty())
                                "Couldn't"
                            else
                                " & couldn't"
                        )
                        append(" open")
                        bold {
                            color(context.getColor(R.color.highlight)) {
                                append(" ${cropResults.nNotOpenableImages}")
                            }
                        }
                        append(" ${"image".numericallyInflected(cropResults.nNotOpenableImages)}")
                    }
                    ifEmpty { null }
                }

        private var showedCropResultsSnackbar: Boolean = false

        /**
         * Other
         */

        val backPressHandler = BackPressListener(
            viewModelScope,
            context.resources.getLong(R.integer.duration_backpress_confirmation_window)
        )
    }

    @Inject
    lateinit var globalFlags: GlobalFlags

    private val viewModel by viewModels<ViewModel>()

    private val activityViewModel by activityViewModels<ExaminationActivity.ViewModel>()

    private lateinit var cropPager: CropPager

    override fun onAttach(context: Context) {
        super.onAttach(context)

        requireActivity().hideSystemBars()
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
            if (singleCropRemaining && lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED))
                binding.allCropsButtonsWLabel.animate(Techniques.ZoomOut)
        }
    }

    private fun FragmentCroppagerBinding.updateOnDataSetPositionChanged(position: Int) {
        with(viewModel.dataSet[position].crop) {
            discardingStatisticsTv.text = resources.getHtmlText(
                R.string.discarding_statistics,
                "$discardedPercentage%",
                discardedFileSizeString
            )
        }

        viewModel.dataSet.pageIndex(position).let { pageIndex ->
            pageIndicationTv.update(pageIndex + 1, viewModel.dataSet.size)
        }
    }

    private fun FragmentCroppagerBinding.updateOnAutoScrollStatusChanged(doAutoScroll: Boolean) {
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
            viewPager.setPageTransformer(CubeOutPageTransformer())
            cancelAutoScrollButton.hide()

            // fade in/show views
            viewModel.autoScrollCoroutine?.let {
                it.cancel()
                snackbarRepelledLayout.fadeIn()
                if (!viewModel.singleCropRemaining)
                    allCropsButtonsWLabel.fadeIn()
            }
                ?: run {
                    snackbarRepelledLayout.show()
                    if (!viewModel.singleCropRemaining)
                        allCropsButtonsWLabel.show()
                }

            // show snackbar if applicable
            viewModel.showCropResultsSnackbarIfApplicable(lifecycleScope, ::getSnackyBuilder)
        }
    }

    private fun FragmentCroppagerBinding.setOnClickListeners() {
        discardAllButton.setOnClickListener {
            castActivity<ExaminationActivity>().invokeSubsequentController(this@CropPagerFragment)
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
                viewModel.dataSet.liveElement.run {
                    CropAdjustmentFragment.getInstance(
                        screenshot.uri,
                        crop.edges
                    )
                },
                true
            )
                .addToBackStack(null)
                .commit()
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
    }

    override fun onCropAdjustment(cropEdges: CropEdges) {
        viewModel.dataSet.liveElement.let {
            it.crop = Crop.fromScreenshot(
                requireContext().contentResolver.loadBitmap(it.screenshot.uri)!!,
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

    private enum class CropProcedure {
        Discard,
        Save
    }

    private fun removeView(dataSetPosition: Int, cropProcedure: CropProcedure) {
        if (viewModel.singleCropRemaining) {
            castActivity<ExaminationActivity>().invokeSubsequentController(this)
            return
        }

        cropPager.scrollToNextViewAndRemoveCurrent(dataSetPosition)
        requireContext().showToast(
            when (cropProcedure) {
                CropProcedure.Discard -> "Discarded crop"
                CropProcedure.Save -> "Saved crop"
            }
        )
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
        castActivity<ExaminationActivity>().invokeSubsequentController(this)
    }

    override val snackbarAnchorView: View
        get() = binding.snackbarRepelledLayout.parent as View

    fun onBackPress() {
        viewModel.backPressHandler(
            {
                requireContext().showToast("Tap again to return to main screen")
            },
            {
                castActivity<ExaminationActivity>().startMainActivity()
            }
        )
    }
}