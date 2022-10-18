package com.autocrop.activities.iodetermination.fragments.croppager

import android.app.Activity.RESULT_OK
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.SpannableStringBuilder
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.text.bold
import androidx.core.text.color
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.transition.TransitionInflater
import com.autocrop.Crop
import com.autocrop.activities.iodetermination.fragments.IODeterminationActivityFragment
import com.autocrop.activities.iodetermination.fragments.croppager.dialogs.AbstractCropDialog
import com.autocrop.activities.iodetermination.fragments.croppager.dialogs.CropDialog
import com.autocrop.activities.iodetermination.fragments.croppager.dialogs.CropEntiretyDialog
import com.autocrop.activities.iodetermination.fragments.croppager.dialogs.InstructionsDialog
import com.autocrop.activities.iodetermination.fragments.croppager.pager.CropPagerAdapter
import com.autocrop.activities.iodetermination.fragments.croppager.pager.CropPagerProxy
import com.autocrop.activities.iodetermination.fragments.croppager.viewmodel.CropPagerViewModel
import com.autocrop.activities.iodetermination.fragments.croppager.viewmodel.Scroller
import com.autocrop.activities.iodetermination.fragments.saveall.SaveAllFragment
import com.autocrop.preferences.BooleanPreferences
import com.autocrop.utils.android.extensions.animate
import com.autocrop.utils.android.extensions.crossFade
import com.autocrop.utils.android.extensions.getThemedColor
import com.autocrop.utils.android.extensions.openBitmap
import com.autocrop.utils.android.extensions.show
import com.autocrop.utils.android.extensions.snacky
import com.autocrop.utils.android.livedata.asMutable
import com.autocrop.utils.kotlin.extensions.executeAsyncTask
import com.autocrop.utils.kotlin.extensions.numericallyInflected
import com.daimajia.androidanimations.library.Techniques
import com.lyrebirdstudio.croppylib.CropEdges
import com.lyrebirdstudio.croppylib.activity.CroppyActivity
import com.w2sv.autocrop.R
import com.w2sv.autocrop.databinding.FragmentCroppagerBinding
import de.mateware.snacky.Snacky

class CropPagerFragment :
    IODeterminationActivityFragment<FragmentCroppagerBinding>(FragmentCroppagerBinding::class.java) {

    private val viewModel by activityViewModels<CropPagerViewModel>()
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
            val saveCrop = bundle.getBoolean(AbstractCropDialog.EXTRA_DIALOG_CONFIRMED)

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
            if (it.getBoolean(AbstractCropDialog.EXTRA_DIALOG_CONFIRMED))
                fragmentHostingActivity
                    .fragmentReplacementTransaction(SaveAllFragment(),true)
                    .commit()
            else
                typedActivity.invokeSubsequentFragment()
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

    private fun CropPagerViewModel.setLiveDataObservers() {
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

                scroller?.let {
                    it.cancel()
                    crossFade(
                        arrayOf(binding.cancelAutoScrollButton),
                        manualScrollingStateViews
                    )
                } ?: manualScrollingStateViews.forEach { it.show() }

                if (!BooleanPreferences.cropPagerInstructionsShown)
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
                                requireContext().getThemedColor(R.color.highlighted_text_view)
                            ) { append(" $nDismissedScreenshots") }
                        }
                        .append(" image".numericallyInflected(nDismissedScreenshots))
                )
                    .setIcon(R.drawable.ic_error_24)
                    .show()
            }
        }
    }

    val croppyActivityLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            result.data?.let {
                processAdjustedCropEdges(CroppyActivity.getCropEdges(it))
                requireActivity().snacky(
                    "Adjusted crop",
                    duration = Snacky.LENGTH_SHORT
                )
                    .setIcon(R.drawable.ic_baseline_done_24)
                    .show()
            }
        }
    }

    /**
     * Set new [Crop] in [viewModel].dataSet.currentPosition and notify [CropPagerAdapter]
     */
    private fun processAdjustedCropEdges(adjustedEdges: CropEdges) {
        with(viewModel.dataSet.currentValue) {
            crop = Crop.fromScreenshot(
                requireContext().contentResolver.openBitmap(screenshot.uri),
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