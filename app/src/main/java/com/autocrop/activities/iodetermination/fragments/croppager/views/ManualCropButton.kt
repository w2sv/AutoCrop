package com.autocrop.activities.iodetermination.fragments.croppager.views

import android.content.Context
import android.util.AttributeSet
import androidx.fragment.app.findFragment
import com.autocrop.activities.iodetermination.fragments.croppager.CropPagerFragment
import com.autocrop.activities.iodetermination.fragments.croppager.viewmodel.CropPagerViewModel
import com.autocrop.ui.elements.ExtendedAppCompatImageButton
import com.autocrop.utils.android.extensions.activityViewModel
import com.blogspot.atifsoftwares.animatoolib.Animatoo
import com.lyrebirdstudio.croppylib.CroppyRequest
import com.lyrebirdstudio.croppylib.CroppyTheme
import com.lyrebirdstudio.croppylib.activity.CroppyActivity
import com.w2sv.autocrop.R

class ManualCropButton(context: Context, attributeSet: AttributeSet):
    ExtendedAppCompatImageButton(context, attributeSet) {

    override fun onClickListener() {
        val cropBundle = activityViewModel<CropPagerViewModel>().dataSet.currentValue

        findFragment<CropPagerFragment>().croppyActivityLauncher.launch(
            CroppyActivity.intent(
                context,
                CroppyRequest(
                    cropBundle.screenshot.uri,
                    cropBundle.crop.edges,
                    cropBundle.screenshot.cropEdgePairCandidates,
                    CroppyTheme(
                        accentColor = R.color.magenta_bright,
                        backgroundColor = R.color.magenta_dark
                    ),
                    Animatoo::animateInAndOut
                )
            )
        )
        Animatoo.animateInAndOut(context)
    }
}