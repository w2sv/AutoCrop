package com.autocrop.activities.examination.fragments.croppager.views

import android.content.Context
import android.util.AttributeSet
import androidx.core.graphics.toRectF
import com.autocrop.activities.examination.ExaminationActivity
import com.autocrop.activities.examination.fragments.croppager.viewmodel.ViewPagerViewModel
import com.autocrop.retriever.activity.ActivityRetriever
import com.autocrop.retriever.activity.ContextBasedActivityRetriever
import com.autocrop.retriever.viewmodel.ViewModelRetriever
import com.autocrop.ui.elements.ExtendedAppCompatImageButton
import com.blogspot.atifsoftwares.animatoolib.Animatoo
import com.lyrebirdstudio.croppylib.CropRequest
import com.lyrebirdstudio.croppylib.CroppyTheme
import com.lyrebirdstudio.croppylib.launchCroppyActivity
import com.w2sv.autocrop.R

class ManualCropButton(context: Context, attributeSet: AttributeSet)
    : ExtendedAppCompatImageButton(context, attributeSet),
    ViewModelRetriever<ViewPagerViewModel> by ViewPagerViewModelRetriever(context),
    ActivityRetriever<ExaminationActivity> by ContextBasedActivityRetriever(context) {

    companion object {
        const val CROPPY_ACTIVITY_REQUEST_CODE = 69
    }

    override fun onClickListener() {
        val transitionAnimation = Animatoo::animateInAndOut
        val cropBundle = sharedViewModel.dataSet.currentValue

        activity.launchCroppyActivity(
            CropRequest(
                cropBundle.screenshot.uri,
                requestCode = CROPPY_ACTIVITY_REQUEST_CODE,
                initialCropRect = cropBundle.crop.rect.toRectF(),
                croppyTheme = CroppyTheme(
                    accentColor = R.color.magenta_bright,
                    backgroundColor = R.color.magenta_dark
                ),
                exitActivityAnimation = transitionAnimation
            )
        )
        transitionAnimation(context)
    }
}