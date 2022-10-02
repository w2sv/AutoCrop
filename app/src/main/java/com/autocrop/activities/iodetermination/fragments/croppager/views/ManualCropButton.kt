package com.autocrop.activities.iodetermination.fragments.croppager.views

import android.content.Context
import android.util.AttributeSet
import androidx.core.graphics.toRectF
import com.autocrop.activities.iodetermination.IODeterminationActivity
import com.autocrop.activities.iodetermination.fragments.croppager.viewmodel.CropPagerViewModel
import com.autocrop.retriever.activity.ActivityRetriever
import com.autocrop.retriever.activity.ContextBasedActivityRetriever
import com.autocrop.ui.elements.ExtendedAppCompatImageButton
import com.autocrop.utils.android.extensions.activityViewModel
import com.blogspot.atifsoftwares.animatoolib.Animatoo
import com.lyrebirdstudio.croppylib.CropRequest
import com.lyrebirdstudio.croppylib.CroppyTheme
import com.lyrebirdstudio.croppylib.launchCroppyActivity
import com.w2sv.autocrop.R

class ManualCropButton(context: Context, attributeSet: AttributeSet)
    : ExtendedAppCompatImageButton(context, attributeSet),
    ActivityRetriever<IODeterminationActivity> by ContextBasedActivityRetriever(context) {

    companion object {
        const val CROPPY_ACTIVITY_REQUEST_CODE = 69
    }

    override fun onClickListener() {
        val cropBundle = activityViewModel<CropPagerViewModel>().dataSet.currentValue

        activity.launchCroppyActivity(
            CropRequest(
                cropBundle.screenshot.uri,
                requestCode = CROPPY_ACTIVITY_REQUEST_CODE,
                initialCropRect = cropBundle.crop.rect.toRectF(),
                croppyTheme = CroppyTheme(
                    accentColor = R.color.magenta_bright,
                    backgroundColor = R.color.magenta_dark
                ),
                exitActivityAnimation = Animatoo::animateInAndOut
            )
        )
        Animatoo.animateInAndOut(context)
    }
}