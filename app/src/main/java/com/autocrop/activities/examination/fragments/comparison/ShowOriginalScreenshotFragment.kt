package com.autocrop.activities.examination.fragments.comparison

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.InsetDrawable
import android.os.Bundle
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.core.view.ViewCompat
import androidx.lifecycle.ViewModelProvider
import androidx.transition.Transition
import androidx.transition.TransitionInflater
import androidx.transition.TransitionListenerAdapter
import com.autocrop.activities.examination.fragments.ExaminationActivityFragment
import com.autocrop.activities.examination.fragments.viewpager.ViewPagerViewModel
import com.autocrop.collections.CropBundle
import com.w2sv.autocrop.databinding.ExaminationFragmentShowOriginalScreenshotBinding

class ShowOriginalScreenshotFragment
    : ExaminationActivityFragment<ExaminationFragmentShowOriginalScreenshotBinding>(ExaminationFragmentShowOriginalScreenshotBinding::class.java){

    private lateinit var transitionListenerAdapter: TransitionListenerAdapter

    override fun onAttach(context: Context) {
        super.onAttach(context)

        val sharedElementTransition = TransitionInflater.from(requireContext())
            .inflateTransition(android.R.transition.move)

        transitionListenerAdapter = object: TransitionListenerAdapter(){
            override fun onTransitionEnd(transition: Transition) {
                super.onTransitionEnd(transition)

                with(binding.iv){
                    layoutParams = preResizeLayoutParameters
                    setImageBitmap(screenshot)
                }
            }
        }

        sharedElementEnterTransition = sharedElementTransition
            .addListener(transitionListenerAdapter)
    }

    override fun onStop() {
        super.onStop()

        binding.iv.setMarginalizedCrop()
        (sharedElementEnterTransition as Transition).removeListener(transitionListenerAdapter)
    }

    private lateinit var cropBundle: CropBundle
    private lateinit var screenshot: Bitmap

    private lateinit var preResizeLayoutParameters: RelativeLayout.LayoutParams

    private var displayingScreenshot = true

    private fun ImageView.setMarginalizedCrop(){
        layoutParams = (layoutParams as RelativeLayout.LayoutParams).apply {
            setMargins(0, cropBundle.topOffset, 0, cropBundle.bottomOffset)
        }
        setImageBitmap(cropBundle.crop)
    }

    override fun onViewCreatedCore(savedInstanceState: Bundle?) {
        cropBundle = ViewModelProvider(requireActivity())[ViewPagerViewModel::class.java].dataSet.currentCropBundle

        ViewCompat.setTransitionName(binding.iv, cropBundle.hashCode().toString())

        screenshot = BitmapFactory.decodeStream(requireContext().contentResolver.openInputStream(cropBundle.screenshotUri))

        with(binding.iv){
            preResizeLayoutParameters = RelativeLayout.LayoutParams(layoutParams)

            setMarginalizedCrop()

            setOnClickListener {
                val insetCrop = InsetDrawable(BitmapDrawable(resources, cropBundle.crop), 0, cropBundle.topOffset, 0, cropBundle.bottomOffset)

                if (displayingScreenshot)
                    setImageDrawable(insetCrop)
                else
                    setImageBitmap(screenshot)

                displayingScreenshot = !displayingScreenshot
            }
        }
    }
}
