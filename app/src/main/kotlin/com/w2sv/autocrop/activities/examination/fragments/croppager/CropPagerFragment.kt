package com.w2sv.autocrop.activities.examination.fragments.croppager

import android.content.Context
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.view.View
import androidx.core.text.bold
import androidx.core.text.color
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import com.daimajia.androidanimations.library.Techniques
import com.w2sv.androidutils.BackPressListener
import com.w2sv.androidutils.extensions.getColoredIcon
import com.w2sv.androidutils.extensions.getHtmlText
import com.w2sv.androidutils.extensions.getLong
import com.w2sv.androidutils.extensions.getThemedColor
import com.w2sv.androidutils.extensions.hide
import com.w2sv.androidutils.extensions.hideSystemBars
import com.w2sv.androidutils.extensions.postValue
import com.w2sv.androidutils.extensions.show
import com.w2sv.androidutils.extensions.viewModel
import com.w2sv.autocrop.R
import com.w2sv.autocrop.activities.AppFragment
import com.w2sv.autocrop.activities.FragmentedActivity
import com.w2sv.autocrop.activities.crop.CropResults
import com.w2sv.autocrop.activities.examination.ExaminationActivity
import com.w2sv.autocrop.activities.examination.fragments.comparison.ComparisonFragment
import com.w2sv.autocrop.activities.examination.fragments.croppager.dialogs.SaveAllCropsDialog
import com.w2sv.autocrop.activities.examination.fragments.croppager.dialogs.SaveCropDialog
import com.w2sv.autocrop.activities.examination.fragments.manualcrop.ManualCropFragment
import com.w2sv.autocrop.activities.examination.fragments.saveall.SaveAllFragment
import com.w2sv.autocrop.activities.getFragmentInstance
import com.w2sv.autocrop.cropbundle.Crop
import com.w2sv.autocrop.cropbundle.cropping.CropEdges
import com.w2sv.autocrop.cropbundle.io.extensions.loadBitmap
import com.w2sv.autocrop.databinding.FragmentCroppagerBinding
import com.w2sv.autocrop.preferences.BooleanPreferences
import com.w2sv.autocrop.preferences.GlobalFlags
import com.w2sv.autocrop.ui.CubeOutPageTransformer
import com.w2sv.autocrop.ui.animate
import com.w2sv.autocrop.ui.currentViewHolder
import com.w2sv.autocrop.ui.fadeIn
import com.w2sv.autocrop.ui.scrollPeriodically
import com.w2sv.autocrop.utils.extensions.snackyBuilder
import com.w2sv.bidirectionalviewpager.recyclerview.ImageViewHolder
import com.w2sv.kotlinutils.delegates.Consumable
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import de.mateware.snacky.Snacky
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class CropPagerFragment :
    AppFragment<FragmentCroppagerBinding>(FragmentCroppagerBinding::class.java),
    SaveCropDialog.ResultListener,
    SaveAllCropsDialog.ResultListener,
    ManualCropFragment.ResultListener {

    companion object {
        fun getInstance(cropResults: CropResults): CropPagerFragment =
            getFragmentInstance(CropPagerFragment::class.java, CropResults.EXTRA to cropResults)
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

        fun getSaveCropDialog(showDismissButton: Boolean): SaveCropDialog =
            SaveCropDialog.getInstance(dataSet.livePosition.value!!, showDismissButton)

        fun getSaveAllCropsDialog(showDismissButton: Boolean): SaveAllCropsDialog =
            SaveAllCropsDialog.getInstance(dataSet.size, showDismissButton)

        /**
         * AutoScroll
         */

        var autoScrollCoroutine: Job? = null

        val doAutoScrollLive: LiveData<Boolean> = MutableLiveData(booleanPreferences.autoScroll && dataSet.size > 1)

        val dialogInflationEnabled: Boolean
            get() = doAutoScrollLive.value == false

        fun maxAutoScrolls(): Int =
            dataSet.size - dataSet.livePosition.value!!

        /**
         * Other
         */

        /**
         * Inherently serves as flag, with != null meaning snackbar is to be displayed and vice-versa
         */
        val uncroppedScreenshotsSnackbarText by Consumable(
            SpannableStringBuilder()
                .run {
                    val cropResults = savedStateHandle.get<CropResults>(CropResults.EXTRA)!!

                    if (cropResults.nNotCroppableImages != 0) {
                        append("Couldn't find crop bounds for")
                        bold {
                            color(context.getThemedColor(R.color.highlight)) {
                                append(" ${cropResults.nNotCroppableImages}")
                            }
                        }
                        append(" screenshot(s)")
                    }

                    when {
                        cropResults.nNotOpenableImages == 0 -> Unit
                        isEmpty() -> {
                            append(
                                "Couldn't open ${cropResults.nNotOpenableImages} image(s)"
                            )
                        }

                        else -> {
                            append(
                                "& couldn't open ${cropResults.nNotOpenableImages} image(s)"
                            )
                        }
                    }
                    ifEmpty { null }
                }
        )

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
                if (viewModel.dialogInflationEnabled)
                    viewModel.getSaveCropDialog(true)
                        .show(childFragmentManager)
            },
            onLongClickListener = {
                if (viewModel.dialogInflationEnabled) {
                    (
                            if (viewModel.singleCropRemaining)
                                viewModel.getSaveCropDialog(true)
                            else
                                viewModel.getSaveAllCropsDialog(true)
                            )
                        .show(childFragmentManager)
                    true
                }
                else
                    false
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
            if (singleCropRemaining)
                binding.allCropsButtonsWLabel.animate(Techniques.ZoomOut)
        }
    }

    private fun FragmentCroppagerBinding.updateOnDataSetPositionChanged(position: Int) {
        with(viewModel.dataSet.liveElement) {
            discardingStatisticsTv.text = resources.getHtmlText(
                R.string.discarding_statistics,
                "${crop.discardedPercentage}%",
                crop.discardedFileSizeString
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

            // launch AutoScroll coroutine
            lifecycleScope.launch {
                val scrollPeriod = resources.getLong(R.integer.period_auto_scroll)

                if (viewModel.autoScrollCoroutine == null)
                    delay(scrollPeriod)

                viewModel.autoScrollCoroutine = viewPager.scrollPeriodically(
                    this,
                    viewModel.maxAutoScrolls(),
                    scrollPeriod
                ) {
                    viewModel.doAutoScrollLive.postValue(false)
                }
            }
        }
        else {
            viewPager.setPageTransformer(CubeOutPageTransformer())
            cancelAutoScrollButton.hide()

            // fade in/show views
            viewModel.autoScrollCoroutine?.let {
                it.cancel()
                snackbarRepelledLayout.fadeIn()
                allCropsButtonsWLabel.fadeIn()
            }
                ?: run {
                    snackbarRepelledLayout.show()
                    if (!viewModel.singleCropRemaining)
                        allCropsButtonsWLabel.show()
                }

            // show snackbar if applicable
            viewModel.uncroppedScreenshotsSnackbarText?.let {
                repelledSnackyBuilder(it)
                    .setIcon(com.w2sv.permissionhandler.R.drawable.ic_error_24)
                    .build()
                    .show()
            }
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
            removeView(viewModel.dataSet.livePosition.value!!)
        }
        saveCropButton.setOnClickListener {
            viewModel.getSaveCropDialog(false)
                .show(childFragmentManager)
        }
        manualCropButton.setOnClickListener {
            (requireActivity() as FragmentedActivity).fragmentReplacementTransaction(
                ManualCropFragment.getInstance(
                    viewModel.dataSet.liveElement
                ),
                true
            )
                .addToBackStack(null)
                .commit()
        }
        comparisonButton.setOnClickListener {
            (requireActivity() as FragmentedActivity).fragmentReplacementTransaction(
                ComparisonFragment.getInstance(viewModel.dataSet.liveElement)
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

    override fun onManualCropResult(cropEdges: CropEdges) {
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
            repelledSnackyBuilder("Adjusted crop")
                .setIcon(requireContext().getColoredIcon(R.drawable.ic_check_24, R.color.success))
                .build()
                .show()
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
        removeView(dataSetPosition)
    }

    override fun onDiscardCrop(dataSetPosition: Int) {
        removeView(dataSetPosition)
    }

    private fun removeView(dataSetPosition: Int) {
        if (viewModel.singleCropRemaining)
            castActivity<ExaminationActivity>().invokeSubsequentController(this)
        else
            cropPager.scrollToNextViewAndRemoveCurrent(dataSetPosition)
    }

    override fun onSaveAllCrops() {
        fragmentHostingActivity()
            .fragmentReplacementTransaction(
                SaveAllFragment.getInstance(ArrayList(viewModel.dataSet.indices.toList())),
                true
            )
            .commit()
    }

    override fun onDiscardAllCrops() {
        castActivity<ExaminationActivity>().invokeSubsequentController(this)
    }

    private fun repelledSnackyBuilder(text: CharSequence): Snacky.Builder =
        requireActivity()
            .snackyBuilder(text)
            .setView(binding.snackbarRepelledLayout.parent as View)

    fun onBackPress() {
        viewModel.backPressHandler(
            {
                repelledSnackyBuilder("Tap again to return to main screen")
                    .build()
                    .show()
            },
            {
                castActivity<ExaminationActivity>().startMainActivity()
            }
        )
    }
}