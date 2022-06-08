package com.lyrebirdstudio.croppylib

import android.app.Activity
import com.lyrebirdstudio.croppylib.activity.CroppyActivity

fun launchCroppyActivity(activity: Activity, cropRequest: CropRequest){
    activity.startActivityForResult(
        CroppyActivity.newIntent(context = activity, cropRequest = cropRequest),
        cropRequest.requestCode
    )
}
