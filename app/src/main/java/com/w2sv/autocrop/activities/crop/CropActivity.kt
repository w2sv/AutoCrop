package com.w2sv.autocrop.activities.crop

import android.net.Uri
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.SavedStateHandle
import com.w2sv.autocrop.activities.crop.fragments.cropping.CropFragment
import com.w2sv.autocrop.activities.crop.fragments.croppingfailed.CroppingFailedFragment
import com.w2sv.autocrop.activities.main.MainActivity
import com.w2sv.autocrop.controller.activity.ApplicationActivity
import com.w2sv.autocrop.cropping.cropbundle.CropBundle
import com.w2sv.bidirectionalviewpager.livedata.MutableListLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

class CropActivity : ApplicationActivity(CropFragment::class.java) {

    companion object {
        const val EXTRA_N_UNCROPPED_IMAGES = "com.w2sv.autocrop.extra.N_UNCROPPED_IMAGES"
    }

    @HiltViewModel
    class ViewModel @Inject constructor(savedStateHandle: SavedStateHandle) : androidx.lifecycle.ViewModel() {

        private val uris: ArrayList<Uri> = savedStateHandle[MainActivity.EXTRA_SELECTED_IMAGE_URIS]!!

        val nImages: Int get() = uris.size
        val nUncroppedImages: Int get() = nImages - liveCropBundles.size

        val imminentUris: List<Uri>
            get() = uris.run {
                subList(liveCropBundles.size, size)
            }

        val liveCropBundles = MutableListLiveData<CropBundle>(mutableListOf())
    }

    /**
     * Directly [startMainActivity] if [CroppingFailedFragment] visible,
     * otherwise only upon confirmed back press
     */
    override val onBackPressedCallback: OnBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            getCurrentFragment().let {
                when (it) {
                    is CroppingFailedFragment -> startMainActivity()
                    is CropFragment -> it.onBackPress()
                    else -> Unit
                }
            }
        }
    }
}