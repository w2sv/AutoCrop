package com.lyrebirdstudio.croppylib

import android.app.Activity
import com.lyrebirdstudio.croppylib.activity.CroppyActivity

fun Activity.launchCroppyActivity(cropRequest: CropRequest){
    startActivityForResult(
        CroppyActivity.newIntent(context = this, cropRequest = cropRequest),
        cropRequest.requestCode
    )
}
