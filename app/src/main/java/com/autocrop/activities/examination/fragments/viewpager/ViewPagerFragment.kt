package com.autocrop.activities.examination.fragments.viewpager

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.transition.TransitionInflater
import androidx.viewpager2.widget.ViewPager2
import com.autocrop.activities.examination.fragments.ExaminationActivityFragment
import com.autocrop.collections.Crop
import com.autocrop.uielements.CubeOutPageTransformer
import com.autocrop.uielements.view.animate
import com.autocrop.uielements.view.crossFade
import com.autocrop.uielements.view.show
import com.autocrop.utilsandroid.mutableLiveData
import com.daimajia.androidanimations.library.Techniques
import com.w2sv.autocrop.databinding.ExaminationFragmentViewpagerBinding

class ViewPagerFragment :
    ExaminationActivityFragment<ExaminationFragmentViewpagerBinding>(ExaminationFragmentViewpagerBinding::class.java) {

    private val viewModel by viewModels<ViewPagerViewModel>(::requireActivity)

    override fun onAttach(context: Context) {
        super.onAttach(context)

        sharedElementReturnTransition = TransitionInflater.from(context)
            .inflateTransition(android.R.transition.move)
    }

    override fun onViewCreatedCore(savedInstanceState: Bundle?) {
        super.onViewCreatedCore(savedInstanceState)

        binding.viewPager.initialize()
        viewModel.setLiveDataObservers()
    }

    fun handleAdjustedCrop(adjustedCrop: Crop) {
        viewModel.dataSet.currentCropBundle.setAdjustedCrop(adjustedCrop)

        with(binding.viewPager.adapter!!){
            notifyItemChanged(binding.viewPager.currentItem)
            if (viewModel.dataSet.size == 2){
                notifyItemChanged(binding.viewPager.currentItem - 2)
                notifyItemChanged(binding.viewPager.currentItem + 2)
            }
        }
    }

    private fun ViewPagerViewModel.setLiveDataObservers() {
        dataSet.currentPosition.observe(viewLifecycleOwner) { position ->
            binding.discardingStatisticsTv.updateText(position)

            dataSet.pageIndex(position).let { pageIndex ->
                binding.pageIndicationTv.updateText(pageIndex)
                binding.pageIndicationBar.update(pageIndex)
            }
        }

        var autoScroller: AutoScroller? = null

        autoScroll.observe(viewLifecycleOwner) { autoScroll ->
            if (autoScroll) {
                binding.cancelAutoScrollButton.show()
                autoScroller = AutoScroller().apply {
                    run(binding.viewPager, maxAutoScrolls){
                        this@setLiveDataObservers.autoScroll.mutableLiveData.postValue(false)
                    }
                }
            } else {
                sharedViewModel.autoScrollingDoneListenerConsumable?.invoke()
                binding.viewPager.setPageTransformer(CubeOutPageTransformer())

                val manualScrollingStateViews = arrayOf<View>(
                    binding.discardingStatisticsTv,
                    binding.menuInflationButton
                )

                autoScroller?.let {
                    it.cancel()
                    crossFade(
                        arrayOf(binding.cancelAutoScrollButton),
                        manualScrollingStateViews
                    )
                } ?: manualScrollingStateViews.forEach { it.show() }
            }

            binding.viewPager.isUserInputEnabled = !autoScroll
        }

        dataSet.observe(viewLifecycleOwner) { dataSet ->
            if (dataSet.size == 1)
                binding.pageIndicationBar.animate(Techniques.ZoomOut)
        }
    }

    private fun ViewPager2.initialize() {
        adapter = CropPagerAdapter(
            this,
            viewModel,
            typedActivity::invokeSubsequentFragment
        )

        registerOnPageChangeCallback(
            PageChangeHandler(
                viewModel
            )
        )

        setCurrentItem(
            viewModel.initialViewPosition(),
            false
        )
    }
}