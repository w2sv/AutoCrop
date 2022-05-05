package com.autocrop.activities.examination.fragments.viewpager

import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import com.autocrop.activities.examination.fragments.ExaminationActivityFragment
import com.autocrop.uielements.setPageTransformer
import com.autocrop.uielements.view.animate
import com.autocrop.uielements.view.crossFade
import com.autocrop.uielements.view.show
import com.daimajia.androidanimations.library.Techniques
import com.w2sv.autocrop.databinding.ExaminationFragmentViewpagerBinding

class ViewPagerFragment:
    ExaminationActivityFragment<ExaminationFragmentViewpagerBinding>(ExaminationFragmentViewpagerBinding::class.java){

    private companion object{
        const val CURRENT_VIEW_PAGER_POSITION = "CURRENT_VIEW_PAGER_POSITION"
    }

    private val viewModel: ViewPagerViewModel by lazy{
        ViewModelProvider(this)[ViewPagerViewModel::class.java]
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.viewPager.initialize(savedInstanceState?.getInt(CURRENT_VIEW_PAGER_POSITION))

        // set LiveData observers
        viewModel.dataSet.position.observe(viewLifecycleOwner){ position ->
            binding.discardingStatisticsTv.updateText(position)

            viewModel.dataSet.pageIndex(position).let{ pageIndex ->
                binding.pageIndicationTv.updateText(pageIndex)
                binding.pageIndicationBar.update(pageIndex, viewModel.scrolledRight.value!!)
            }
        }

        val scroller = Scroller(viewModel.autoScroll)

        viewModel.autoScroll.observe(viewLifecycleOwner){ autoScroll ->
            if (!autoScroll){
                sharedViewModel.consumeAutoScrollingDoneListenerIfSet()
                binding.viewPager.setPageTransformer()

                if (!viewModel.autoScrolledInitially){
                    binding.discardingStatisticsTv.show()
                    binding.buttonToolbar.show()
                }
                else {
                    scroller.cancel()
                    crossFade(
                        binding.autoScrollingTextView,
                        binding.discardingStatisticsTv, binding.buttonToolbar
                    )
                }
            }
            else{
                binding.autoScrollingTextView.show()
                scroller.run(binding.viewPager, viewModel.maxScrolls())
            }
        }

        viewModel.dataSet.observe(viewLifecycleOwner){ dataSet ->
            if (dataSet.size == 1)
                binding.pageIndicationLayout.animate(Techniques.ZoomOut)
        }

        if (viewModel.dataSet.size > 1)
            binding.pageIndicationLayout.show()
    }

    private fun ViewPager2.initialize(previousPosition: Int?){
        // set adapter + first view
        adapter = CropPagerAdapter(
            this,
            viewModel,
            castedActivity::invokeSubsequentFragment
        )

        setCurrentItem(
            previousPosition ?: viewModel.initialViewPosition,
            false
        )

        // register onPageChangeCallbacks
        registerOnPageChangeCallback(PageChangeHandler(viewModel))
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putInt(CURRENT_VIEW_PAGER_POSITION, binding.viewPager.currentItem)
    }
}