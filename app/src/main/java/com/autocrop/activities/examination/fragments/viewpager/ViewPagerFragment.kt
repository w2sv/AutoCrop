package com.autocrop.activities.examination.fragments.viewpager

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.transition.TransitionInflater
import androidx.viewpager2.widget.ViewPager2
import com.autocrop.activities.examination.fragments.ExaminationActivityFragment
import com.autocrop.uielements.CubeOutPageTransformer
import com.autocrop.uielements.view.animate
import com.autocrop.uielements.view.crossFade
import com.autocrop.uielements.view.show
import com.daimajia.androidanimations.library.Techniques
import com.w2sv.autocrop.databinding.ExaminationFragmentViewpagerBinding

class ViewPagerFragment:
    ExaminationActivityFragment<ExaminationFragmentViewpagerBinding>(ExaminationFragmentViewpagerBinding::class.java){

    private val viewModel: ViewPagerViewModel by lazy{
        ViewModelProvider(requireActivity())[ViewPagerViewModel::class.java]
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        sharedElementEnterTransition = TransitionInflater.from(requireContext())
            .inflateTransition(android.R.transition.move)
        sharedElementReturnTransition = TransitionInflater.from(requireContext())
            .inflateTransition(android.R.transition.move)
    }

    override fun onViewCreatedCore(savedInstanceState: Bundle?) {
        super.onViewCreatedCore(savedInstanceState)

        binding.viewPager.initialize()
        setLiveDataObservers()

        if (!viewModel.dataSet.containsSingleElement)
            binding.pageIndicationLayout.show()
    }

    private fun setLiveDataObservers(){
        viewModel.dataSet.position.observe(viewLifecycleOwner){ position ->
            binding.discardingStatisticsTv.updateText(position)

            viewModel.dataSet.pageIndex(position).let{ pageIndex ->
                binding.pageIndicationTv.updateText(pageIndex)
                binding.pageIndicationBar.update(pageIndex, viewModel.scrolledRight)
            }
        }

        var scroller: Scroller? = null

        viewModel.autoScroll.observe(viewLifecycleOwner){ autoScroll ->
            if (autoScroll){
                binding.autoScrollingTextView.show()
                scroller = Scroller(viewModel.autoScroll).apply {
                    run(binding.viewPager, viewModel.maxAutoScrolls())
                }
            }
            else{
                sharedViewModel.autoScrollingDoneListenerConsumable?.let { it() }
                binding.viewPager.setPageTransformer(CubeOutPageTransformer())

                if (scroller is Scroller){
                    scroller!!.cancel()
                    crossFade(
                        arrayOf(binding.autoScrollingTextView),
                        arrayOf(binding.discardingStatisticsTv, binding.buttonToolbar)
                    )
                }
                else{
                    binding.discardingStatisticsTv.show()
                    binding.buttonToolbar.show()
                }
            }
        }

        viewModel.dataSet.observe(viewLifecycleOwner){ dataSet ->
            if (dataSet.size == 1)
                binding.pageIndicationLayout.animate(Techniques.ZoomOut)
        }
    }

    private fun ViewPager2.initialize(){
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