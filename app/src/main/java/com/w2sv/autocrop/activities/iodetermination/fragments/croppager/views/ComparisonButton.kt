package com.w2sv.autocrop.activities.iodetermination.fragments.croppager.views

import android.content.Context
import android.util.AttributeSet
import androidx.fragment.app.findFragment
import com.w2sv.autocrop.activities.iodetermination.fragments.comparison.ComparisonFragment
import com.w2sv.autocrop.activities.iodetermination.fragments.croppager.CropPager
import com.w2sv.autocrop.activities.iodetermination.fragments.croppager.CropPagerFragment
import com.w2sv.autocrop.activities.iodetermination.fragments.croppager.viewmodel.CropPagerViewModel
import com.w2sv.autocrop.controller.activity.retriever.ActivityRetriever
import com.w2sv.autocrop.controller.activity.retriever.ContextBasedActivityRetriever
import com.w2sv.autocrop.ui.views.ExtendedAppCompatImageButton
import com.w2sv.autocrop.utils.android.extensions.viewModelLazy
import com.w2sv.bidirectionalviewpager.viewpager.currentViewHolder

class ComparisonButton(context: Context, attributeSet: AttributeSet) :
    ExtendedAppCompatImageButton(context, attributeSet),
    ActivityRetriever by ContextBasedActivityRetriever(context) {

    private val viewModel by viewModelLazy<CropPagerViewModel>()

    override fun onClickListener() {
        fragmentHostingActivity.fragmentReplacementTransaction(
            ComparisonFragment.instance(viewModel.dataSet.liveElement)
        )
            .addToBackStack(null)
            .apply {
                val cropImageView =
                    findFragment<CropPagerFragment>()
                        .binding
                        .viewPager
                        .currentViewHolder<CropPager.Adapter.ViewHolder>()
                        .imageView

                addSharedElement(
                    cropImageView,
                    cropImageView.transitionName
                )
            }
            .commit()
    }
}