package com.autocrop.activities.examination.fragments.viewpager

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.viewModels
import androidx.transition.TransitionInflater
import androidx.viewpager2.widget.ViewPager2
import com.autocrop.activities.examination.fragments.ExaminationActivityFragment
import com.autocrop.collections.Crop
import com.autocrop.uielements.CubeOutPageTransformer
import com.autocrop.uielements.view.animate
import com.autocrop.uielements.view.crossFade
import com.autocrop.uielements.view.show
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
        setLiveDataObservers()
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

    private fun setLiveDataObservers() {
        viewModel.dataSet.position.observe(viewLifecycleOwner) { position ->
            binding.discardingStatisticsTv.updateText(position)

            viewModel.dataSet.pageIndex(position).let { pageIndex ->
                binding.pageIndicationTv.updateText(pageIndex)
                binding.pageIndicationBar.update(pageIndex, viewModel.scrolledRight)
            }
        }

        var scroller: Scroller? = null

        viewModel.autoScroll.observe(viewLifecycleOwner) { autoScroll ->
            if (autoScroll) {
                binding.cancelAutoScrollButton.show()
                binding.viewPager.isUserInputEnabled = false
                scroller = Scroller(viewModel.autoScroll).apply {
                    run(binding.viewPager, viewModel.maxAutoScrolls())
                }
            } else {
                sharedViewModel.autoScrollingDoneListenerConsumable?.invoke()

                binding.viewPager.setPageTransformer(CubeOutPageTransformer())
                binding.viewPager.isUserInputEnabled = true

                if (scroller != null) {
                    scroller!!.cancel()
                    crossFade(
                        arrayOf(binding.cancelAutoScrollButton),
                        arrayOf(
                            binding.discardingStatisticsTv,
                            binding.menuInflationButton
                        )
                    )
                } else {
                    binding.discardingStatisticsTv.show()
                    binding.menuInflationButton.show()
                }
            }
        }

        viewModel.dataSet.observe(viewLifecycleOwner) { dataSet ->
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