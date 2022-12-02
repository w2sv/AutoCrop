package com.w2sv.autocrop.activities.cropexamination.fragments.croppager

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
import com.w2sv.androidutils.extensions.hideSystemBars
import com.w2sv.androidutils.extensions.launchDelayed
import com.w2sv.androidutils.extensions.postValue
import com.w2sv.androidutils.extensions.show
import com.w2sv.autocrop.R
import com.w2sv.autocrop.activities.ApplicationFragment
import com.w2sv.autocrop.activities.crop.CropActivity
import com.w2sv.autocrop.activities.cropexamination.CropExaminationActivity
import com.w2sv.autocrop.activities.cropexamination.fragments.croppager.dialogs.CropDialog
import com.w2sv.autocrop.activities.cropexamination.fragments.croppager.dialogs.CropEntiretyDialog
import com.w2sv.autocrop.activities.cropexamination.fragments.croppager.dialogs.CropPagerInstructionsDialog
import com.w2sv.autocrop.activities.cropexamination.fragments.manualcrop.ManualCropFragment
import com.w2sv.autocrop.activities.cropexamination.fragments.saveall.SaveAllFragment
import com.w2sv.autocrop.cropbundle.Crop
import com.w2sv.autocrop.cropbundle.cropping.CropEdges
import com.w2sv.autocrop.cropbundle.io.extensions.loadBitmap
import com.w2sv.autocrop.databinding.FragmentCroppagerBinding
import com.w2sv.autocrop.preferences.BooleanPreferences
import com.w2sv.autocrop.ui.CubeOutPageTransformer
import com.w2sv.autocrop.ui.animate
import com.w2sv.autocrop.ui.crossFade
import com.w2sv.autocrop.ui.scrollPeriodically
import com.w2sv.autocrop.utils.extensions.snackyBuilder
import com.w2sv.bidirectionalviewpager.BidirectionalViewPagerDataSet
import com.w2sv.kotlinutils.delegates.Consumable
import com.w2sv.kotlinutils.extensions.numericallyInflected
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
    ApplicationFragment<FragmentCroppagerBinding>(FragmentCroppagerBinding::class.java),
    CropDialog.ResultListener,
    CropEntiretyDialog.ResultListener,
    ManualCropFragment.ResultListener,
    CropPagerInstructionsDialog.OnDismissedListener {

    companion object {
        fun getInstance(nUncroppedScreenshots: Int): CropPagerFragment =
            CropPagerFragment()
                .apply {
                    arguments = bundleOf(CropActivity.EXTRA_N_UNCROPPED_SCREENSHOTS to nUncroppedScreenshots)
                }
    }

    @HiltViewModel
    class ViewModel @Inject constructor(
        savedStateHandle: SavedStateHandle,
        booleanPreferences: BooleanPreferences,
        @ApplicationContext context: Context
    ) : androidx.lifecycle.ViewModel() {

        val dataSet = BidirectionalViewPagerDataSet(CropExaminationActivity.ViewModel.cropBundles)

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
        val uncroppedScreenshotsSnackbarText by Consumable<SpannableStringBuilder>(
            savedStateHandle.get<Int>(CropActivity.EXTRA_N_UNCROPPED_SCREENSHOTS)!!.let { nUncroppedScreenshots ->
                if (nUncroppedScreenshots != 0)
                    SpannableStringBuilder()
                        .append("Couldn't find crop bounds for")
                        .bold {
                            color(context.getThemedColor(R.color.highlight)) {
                                append(" $nUncroppedScreenshots")
                            }
                        }
                        .append(" image".numericallyInflected(nUncroppedScreenshots))
                else
                    null
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
    lateinit var booleanPreferences: BooleanPreferences

    private val viewModel by viewModels<ViewModel>()
    private val activityViewModel by activityViewModels<CropExaminationActivity.ViewModel>()

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
    }

    private fun ViewModel.setLiveDataObservers() {
        dataSet.livePosition.observe(viewLifecycleOwner) { position ->
            binding.discardingStatisticsTv.update(position)

            dataSet.pageIndex(position).let { pageIndex ->
                binding.pageIndicationTv.update(pageIndex + 1)
                binding.pageIndicationBar.update(pageIndex)
            }
        }

        doAutoScrollLive.observe(viewLifecycleOwner) { autoScroll ->
            if (autoScroll) {
                binding.cancelAutoScrollButton.show()
                lifecycleScope.launch {
                    val scrollPeriod = resources.getLong(R.integer.delay_large)

                    if (autoScrollCoroutine == null)
                        delay(scrollPeriod)

                    autoScrollCoroutine = binding.viewPager.scrollPeriodically(
                        lifecycleScope,
                        getNAutoScrolls(),
                        scrollPeriod
                    ) {
                        doAutoScrollLive.postValue(false)
                    }
                }
            }
            else {
                binding.viewPager.setPageTransformer(CubeOutPageTransformer())

                autoScrollCoroutine?.let {
                    it.cancel()
                    crossFade(
                        binding.cancelAutoScrollButton,
                        binding.snackbarRepelledLayout
                    )
                }
                    ?: binding.snackbarRepelledLayout.show()

                if (!booleanPreferences.cropPagerInstructionsShown)
                    lifecycleScope.launchDelayed(resources.getLong(R.integer.delay_medium)) {
                        CropPagerInstructionsDialog()
                            .show(childFragmentManager)
                    }
                else
                    showUncroppableScreenshotsSnackbarIfApplicable()

            }
            binding.viewPager.isUserInputEnabled = !autoScroll
        }

        dataSet.observe(viewLifecycleOwner) { dataSet ->
            if (dataSet.size == 1)
                binding.pageIndicationBar.animate(Techniques.ZoomOut)
        }
    }

    override fun onDismissedCropPagerInstructionsDialog() {
        showUncroppableScreenshotsSnackbarIfApplicable()
    }

    private fun showUncroppableScreenshotsSnackbarIfApplicable() {
        viewModel.uncroppedScreenshotsSnackbarText?.let {
            getSnackyBuilder(it)
                .setIcon(com.w2sv.permissionhandler.R.drawable.ic_error_24)
                .build()
                .show()
        }
    }

    override fun onManualCropResult(cropEdges: CropEdges) {
        viewModel.dataSet.liveElement.let {
            it.crop = Crop.fromScreenshot(
                requireContext().contentResolver.loadBitmap(it.screenshot.uri),
                it.screenshot.mediaStoreData.diskUsage,
                cropEdges
            )
        }

        cropPager.adapter.notifyItemChanged(
            binding.viewPager.currentItem,
            viewModel.dataSet.size
        )

        launchAfterShortDelay {
            getSnackyBuilder("Adjusted crop")
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
            activityViewModel.launchViewModelScopedCropProcessingCoroutine(
                dataSetPosition,
                requireContext().applicationContext
            )

        if (viewModel.dataSet.size == 1)
            castActivity<CropExaminationActivity>().replaceWithSubsequentFragment()
        else
            cropPager.removeView(dataSetPosition)
    }

    override fun onCropEntiretyResult(confirmed: Boolean) {
        if (confirmed)
            getFragmentHostingActivity()
                .fragmentReplacementTransaction(SaveAllFragment(), true)
                .commit()
        else
            castActivity<CropExaminationActivity>().replaceWithSubsequentFragment()
    }

    private fun getSnackyBuilder(text: CharSequence): Snacky.Builder =
        requireActivity()
            .snackyBuilder(text)
            .setView(binding.snackbarRepelledHostingLayout)

    fun onBackPress() {
        viewModel.backPressHandler(
            {
                getSnackyBuilder("Tap again to return to main screen")
                    .build()
                    .show()
            },
            {
                castActivity<CropExaminationActivity>().startMainActivity()
            }
        )
    }
}