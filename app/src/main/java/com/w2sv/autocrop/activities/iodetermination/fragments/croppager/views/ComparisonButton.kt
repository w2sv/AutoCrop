package com.w2sv.autocrop.activities.iodetermination.fragments.croppager.views

import android.content.Context
import android.util.AttributeSet
import com.w2sv.autocrop.activities.iodetermination.IODeterminationActivity
import com.w2sv.autocrop.activities.iodetermination.fragments.comparison.ComparisonFragment
import com.w2sv.autocrop.activities.iodetermination.fragments.croppager.CropPagerFragment
import com.w2sv.autocrop.activities.iodetermination.fragments.croppager.pager.CropPagerAdapter
import com.w2sv.autocrop.activities.iodetermination.fragments.croppager.viewmodel.CropPagerViewModel
import com.w2sv.autocrop.controller.activity.retriever.ActivityRetriever
import com.w2sv.autocrop.controller.activity.retriever.ContextBasedActivityRetriever
import com.w2sv.autocrop.ui.views.ExtendedAppCompatImageButton
import com.w2sv.autocrop.utils.android.extensions.recyclerView
import com.w2sv.autocrop.utils.android.extensions.viewModelLazy

class ComparisonButton(context: Context, attributeSet: AttributeSet) :
    ExtendedAppCompatImageButton(context, attributeSet),
    ActivityRetriever<IODeterminationActivity> by ContextBasedActivityRetriever(context) {

    private val viewModel by viewModelLazy<CropPagerViewModel>()

    override fun onClickListener() {
        fragmentHostingActivity.fragmentReplacementTransaction(
            ComparisonFragment.instance(viewModel.dataSet.currentElement)
        )
            .addToBackStack(null)
            .apply {
                val cropImageView =
                    fragmentHostingActivity
                        .getCastCurrentFragment<CropPagerFragment>()!!
                        .binding
                        .viewPager
                        .run {
                            (recyclerView.findViewHolderForAdapterPosition(currentItem) as CropPagerAdapter.CropViewHolder)
                                .imageView
                        }

                addSharedElement(
                    cropImageView,
                    cropImageView.transitionName
                )
            }
            .commit()
    }
}