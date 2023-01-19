package com.w2sv.autocrop.activities.examination.fragments.croppager

import android.content.Context
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.view.View
import androidx.core.os.bundleOf
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
import com.w2sv.androidutils.extensions.getLong
import com.w2sv.androidutils.extensions.getThemedColor
import com.w2sv.androidutils.extensions.hide
import com.w2sv.androidutils.extensions.hideSystemBars
import com.w2sv.androidutils.extensions.launchDelayed
import com.w2sv.androidutils.extensions.postValue
import com.w2sv.androidutils.extensions.show
import com.w2sv.androidutils.extensions.viewModel
import com.w2sv.autocrop.R
import com.w2sv.autocrop.activities.AppFragment
import com.w2sv.autocrop.activities.FragmentedActivity
import com.w2sv.autocrop.activities.crop.CropResults
import com.w2sv.autocrop.activities.examination.ExaminationActivity
import com.w2sv.autocrop.activities.examination.fragments.comparison.ComparisonFragment
import com.w2sv.autocrop.activities.examination.fragments.croppager.dialogs.CropDialog
import com.w2sv.autocrop.activities.examination.fragments.croppager.dialogs.CropEntiretyDialog
import com.w2sv.autocrop.activities.examination.fragments.croppager.dialogs.CropPagerInstructionsDialog
import com.w2sv.autocrop.activities.examination.fragments.manualcrop.ManualCropFragment
import com.w2sv.autocrop.activities.examination.fragments.saveall.SaveAllFragment
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
    CropDialog.ResultListener,
    CropEntiretyDialog.ResultListener,
    ManualCropFragment.ResultListener,
    CropPagerInstructionsDialog.OnDismissedListener {

    companion object {
        fun getInstance(cropResults: CropResults): CropPagerFragment =
            CropPagerFragment()
                .apply {
                    arguments = bundleOf(
                        CropResults.EXTRA to cropResults
                    )
                }
    }

    @HiltViewModel
    class ViewModel @Inject constructor(
        savedStateHandle: SavedStateHandle,
        booleanPreferences: BooleanPreferences,
        @ApplicationContext context: Context
    ) : androidx.lifecycle.ViewModel() {

        val dataSet = CropPager.DataSet(ExaminationActivity.ViewModel.cropBundles)

        val backPressHandler = BackPressListener(
            viewModelScope,
            context.resources.getLong(R.integer.duration_backpress_confirmation_window)
        )

        //$$$$$$$$$$$$$$$$$$$$$$$$
        // Uncropped Screenshots $
        //$$$$$$$$$$$$$$$$$$$$$$$$

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

        //$$$$$$$$$$$$$
        // AutoScroll $
        //$$$$$$$$$$$$$

        var autoScrollCoroutine: Job? = null

        val doAutoScrollLive: LiveData<Boolean> = MutableLiveData(booleanPreferences.autoScroll && dataSet.size > 1)

        fun getNAutoScrolls(): Int =
            dataSet.size - dataSet.livePosition.value!!
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
            viewModel.dataSet
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

        dataSet.observe(viewLifecycleOwner) { dataSet ->
            if (dataSet.size == 1)
                binding.pageIndicationBar.animate(Techniques.ZoomOut)
        }
    }

    private fun FragmentCroppagerBinding.updateOnDataSetPositionChanged(position: Int) {
        discardingStatisticsTv.update(position)

        viewModel.dataSet.pageIndex(position).let { pageIndex ->
            pageIndicationTv.update(pageIndex + 1, viewModel.dataSet.size)
            pageIndicationBar.update(pageIndex)
        }
    }

    private fun FragmentCroppagerBinding.updateOnAutoScrollStatusChanged(doAutoScroll: Boolean) {
        if (doAutoScroll) {
            cancelAutoScrollButton.show()
            lifecycleScope.launch {
                if (viewModel.autoScrollCoroutine == null)
                    delay(resources.getLong(R.integer.period_auto_scroll))

                viewModel.autoScrollCoroutine = binding.viewPager.scrollPeriodically(
                    lifecycleScope,
                    viewModel.getNAutoScrolls(),
                    resources.getLong(R.integer.period_auto_scroll)
                ) {
                    viewModel.doAutoScrollLive.postValue(false)
                }
            }
        }
        else {
            viewPager.setPageTransformer(CubeOutPageTransformer())

            viewModel.autoScrollCoroutine?.let {
                it.cancel()
                cancelAutoScrollButton.hide()
                snackbarRepelledLayout.fadeIn()
            }
                ?: snackbarRepelledLayout.show()

            if (!globalFlags.cropPagerInstructionsShown)
                lifecycleScope.launchDelayed(resources.getLong(R.integer.delay_medium)) {
                    CropPagerInstructionsDialog()
                        .show(childFragmentManager)
                }
            else
                showUncroppableScreenshotsSnackbarIfApplicable()

        }
        viewPager.isUserInputEnabled = !doAutoScroll
    }

    private fun FragmentCroppagerBinding.setOnClickListeners() {
        cancelAutoScrollButton.setOnClickListener {
            viewModel.doAutoScrollLive.postValue(false)
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
                            .currentViewHolder<CropPager.Adapter.ViewHolder>()!!
                            .imageView

                    addSharedElement(
                        cropImageView,
                        cropImageView.transitionName
                    )
                }
                .commit()
        }
    }

    override fun onDismissedCropPagerInstructionsDialog() {
        showUncroppableScreenshotsSnackbarIfApplicable()
    }

    private fun showUncroppableScreenshotsSnackbarIfApplicable() {
        viewModel.uncroppedScreenshotsSnackbarText?.let {
            repelledSnackyBuilder(it)
                .setIcon(com.w2sv.permissionhandler.R.drawable.ic_error_24)
                .build()
                .show()
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

        cropPager.adapter.notifyItemChanged(
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
    override fun onCropDialogResult(confirmed: Boolean, dataSetPosition: Int) {
        if (confirmed)
            activityViewModel.processCropBundleAsScopedCoroutine(
                dataSetPosition,
                requireContext().applicationContext
            )

        if (viewModel.dataSet.size == 1)
            castActivity<ExaminationActivity>().invokeSubsequentController(this)
        else
            cropPager.scrollToNextViewAndRemoveCurrent(dataSetPosition)
    }

    override fun onCropEntiretyDialogResult(confirmed: Boolean) {
        if (confirmed)
            fragmentHostingActivity()
                .fragmentReplacementTransaction(
                    SaveAllFragment.getInstance(ArrayList(viewModel.dataSet.indices.toList())),
                    true
                )
                .commit()
        else
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