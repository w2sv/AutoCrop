package com.autocrop.activities.examination.fragments.croppager.viewmodel

import com.autocrop.dataclasses.Crop
import com.autocrop.dataclasses.CropBundle

class CropBundleDataSet(dataSet: MutableList<CropBundle>): BidirectionalViewPagerDataSet<CropBundle>(dataSet){
    fun replaceCurrentCrop(crop: Crop){
        this[currentPosition.value!!] = CropBundle(currentValue.screenshot, crop)
    }
}