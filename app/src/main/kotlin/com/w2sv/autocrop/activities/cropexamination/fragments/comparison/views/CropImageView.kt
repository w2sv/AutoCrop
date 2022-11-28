package com.w2sv.autocrop.activities.cropexamination.fragments.comparison.views

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.view.ViewCompat
import com.w2sv.androidutils.extensions.viewModel
import com.w2sv.autocrop.activities.cropexamination.fragments.comparison.ComparisonFragment

class CropImageView(context: Context, attributeSet: AttributeSet) : AppCompatImageView(context, attributeSet) {

    private val viewModel by viewModel<ComparisonFragment.ViewModel>()

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        if (!isInEditMode) {
            ViewCompat.setTransitionName(this, viewModel.cropBundle.identifier())
            setImageBitmap(viewModel.cropBundle.crop.bitmap)
        }
    }
}