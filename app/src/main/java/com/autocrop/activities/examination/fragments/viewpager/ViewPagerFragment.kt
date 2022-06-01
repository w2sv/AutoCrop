package com.autocrop.activities.examination.fragments.viewpager

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.ImageView
import androidx.lifecycle.ViewModelProvider
import androidx.transition.TransitionInflater
import androidx.viewpager2.widget.ViewPager2
import com.autocrop.activities.examination.fragments.ExaminationActivityFragment
import com.autocrop.uielements.CubeOutPageTransformer
import com.autocrop.uielements.view.animate
import com.autocrop.uielements.view.crossFade
import com.autocrop.uielements.view.show
import com.daimajia.androidanimations.library.Techniques
import com.lyrebirdstudio.croppylib.Croppy
import com.lyrebirdstudio.croppylib.main.CropRequest
import com.w2sv.autocrop.databinding.ExaminationFragmentViewpagerBinding
import java.io.InputStream

class ViewPagerFragment:
    ExaminationActivityFragment<ExaminationFragmentViewpagerBinding>(ExaminationFragmentViewpagerBinding::class.java){

    private val viewModel: ViewPagerViewModel by lazy{
        ViewModelProvider(requireActivity())[ViewPagerViewModel::class.java]
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        sharedElementReturnTransition = TransitionInflater.from(requireContext())
            .inflateTransition(android.R.transition.move)
    }

    companion object{
        const val MANUAL_CROP_REQUEST_CODE = 69
    }

    override fun onViewCreatedCore(savedInstanceState: Bundle?) {
        super.onViewCreatedCore(savedInstanceState)

        binding.viewPager.initialize()
        setLiveDataObservers()

        binding.croppingButton?.setOnClickListener {
            Croppy.start(
                requireActivity(),
                CropRequest.Auto(
                    viewModel.dataSet.currentCropBundle.screenshotUri,
                    requestCode = MANUAL_CROP_REQUEST_CODE
                )
            )
        }

        if (!viewModel.dataSet.containsSingleElement)
            binding.pageIndicationBar.show()
    }

    fun handleConfiguredCrop(configuredCrop: Bitmap){
        viewModel.dataSet.currentCropBundle.crop = configuredCrop
        binding.viewPager.adapter!!.notifyItemChanged(viewModel.dataSet.position.value!!)
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
                binding.autoScrollingTv.show()
                scroller = Scroller(viewModel.autoScroll).apply {
                    run(binding.viewPager, viewModel.maxAutoScrolls())
                }
            }
            else{
                sharedViewModel.autoScrollingDoneListenerConsumable?.let { it() }
                binding.viewPager.setPageTransformer(CubeOutPageTransformer())

                if (scroller != null){
                    scroller!!.cancel()
                    crossFade(
                        arrayOf(binding.autoScrollingTv),
                        arrayOf(binding.discardingStatisticsTv, binding.buttonToolbar, binding.compareButton)
                    )
                }
                else{
                    with(binding){
                        listOf(discardingStatisticsTv, buttonToolbar, compareButton)
                            .forEach {
                                it.show()
                            }
                    }
                }
            }
        }

        viewModel.dataSet.observe(viewLifecycleOwner){ dataSet ->
            if (dataSet.size == 1)
                binding.pageIndicationBar.animate(Techniques.ZoomOut)
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