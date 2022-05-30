package com.autocrop.activities.examination.fragments.comparison

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.RelativeLayout
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.transition.TransitionInflater
import com.autocrop.activities.examination.fragments.ExaminationActivityFragment
import com.autocrop.activities.examination.fragments.viewpager.ViewPagerViewModel
import com.autocrop.collections.CropBundle
import com.autocrop.uielements.view.crossFade
import com.autocrop.uielements.view.remove
import com.autocrop.uielements.view.show
import com.w2sv.autocrop.databinding.ExaminationFragmentShowOriginalScreenshotBinding

class ShowOriginalScreenshotFragment
    : ExaminationActivityFragment<ExaminationFragmentShowOriginalScreenshotBinding>(ExaminationFragmentShowOriginalScreenshotBinding::class.java){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedElementEnterTransition = TransitionInflater.from(requireContext())
            .inflateTransition(android.R.transition.move)
    }

    private lateinit var cropBundle: CropBundle
    private lateinit var screenshot: Bitmap

    override fun onViewCreatedCore(savedInstanceState: Bundle?) {
        super.onViewCreatedCore(savedInstanceState)

        cropBundle = ViewModelProvider(requireActivity())[ViewPagerViewModel::class.java].dataSet.currentCropBundle
        screenshot = BitmapFactory.decodeStream(requireContext().contentResolver.openInputStream(cropBundle.screenshotUri))

//        binding.cropIv.layoutParams = binding.cropIv.layoutParams.apply {
//            (this as RelativeLayout.LayoutParams).setMargins(0, cropBundle.topOffset, 0, cropBundle.bottomOffset)
//        }

        binding.cropIv.setImageBitmap(cropBundle.crop)
        binding.screenshotIv.setImageBitmap(screenshot)

        binding.root.setOnClickListener {
            if (binding.cropIv.isVisible){
                binding.cropIv.remove()
                binding.screenshotIv.show()
            }
            else{
                binding.screenshotIv.remove()
                binding.cropIv.show()
            }
        }
    }

    override fun onStop() {
        super.onStop()

        binding.screenshotIv.remove()
        binding.cropIv.show()
    }
}
