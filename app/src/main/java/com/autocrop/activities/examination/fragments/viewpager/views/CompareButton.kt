package com.autocrop.activities.examination.fragments.viewpager.views

import android.content.Context
import android.util.AttributeSet
import android.widget.ImageView
import androidx.appcompat.widget.AppCompatButton
import com.autocrop.activities.examination.ExaminationActivity
import com.autocrop.activities.examination.fragments.comparison.ComparisonFragment
import com.autocrop.uielements.view.ActivityRetriever
import com.autocrop.uielements.view.ContextBasedActivityRetriever
import com.w2sv.autocrop.R

class CompareButton(context: Context, attributeSet: AttributeSet):
    AppCompatButton(context, attributeSet),
    ActivityRetriever<ExaminationActivity> by ContextBasedActivityRetriever(context) {

    init {
        setOnClickListener {
            typedActivity.replaceCurrentFragmentWith(
                ComparisonFragment(),
                addToBackStack = true
            ){ fragmentTransaction ->
                val cropImageView = findViewById<ImageView>(R.id.crop_iv)
                fragmentTransaction.addSharedElement(
                    cropImageView,
                    cropImageView.transitionName
                )
            }
        }
    }
}