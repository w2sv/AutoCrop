package com.w2sv.autocrop.activities.examination.fragments.croppager.views

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageButton
import androidx.fragment.app.findFragment
import com.w2sv.androidutils.extensions.viewModel
import com.w2sv.autocrop.activities.FragmentHostingActivity
import com.w2sv.autocrop.activities.examination.fragments.comparison.ComparisonFragment
import com.w2sv.autocrop.activities.examination.fragments.croppager.CropPager
import com.w2sv.autocrop.activities.examination.fragments.croppager.CropPagerFragment
import com.w2sv.autocrop.ui.currentViewHolder

class ComparisonLaunchButton(context: Context, attributeSet: AttributeSet) :
    AppCompatImageButton(context, attributeSet),
    FragmentHostingActivity.Retriever by FragmentHostingActivity.Retriever.Implementation(context) {

    private val viewModel by viewModel<CropPagerFragment.ViewModel>()

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        if (!isInEditMode) {
            setOnClickListener {
                launchComparisonFragment()
            }
        }
    }

    private fun launchComparisonFragment() {
        fragmentHostingActivity.fragmentReplacementTransaction(
            ComparisonFragment.getInstance(viewModel.dataSet.liveElement)
        )
            .addToBackStack(null)
            .apply {
                val cropImageView =
                    findFragment<CropPagerFragment>()
                        .binding
                        .viewPager
                        .currentViewHolder<CropPager.Adapter.ViewHolder>()!!
                        .imageView

                addSharedElement(
                    cropImageView,
                    cropImageView.transitionName
                )
            }
            .commit()
    }
}