package com.autocrop.activities.iodetermination.fragments.croppager.views

import android.content.Context
import android.util.AttributeSet
import com.autocrop.activities.iodetermination.IODeterminationActivity
import com.autocrop.activities.iodetermination.fragments.comparison.ComparisonFragment
import com.autocrop.activities.iodetermination.fragments.croppager.CropPagerFragment
import com.autocrop.activities.iodetermination.fragments.croppager.pager.CropPagerAdapter
import com.autocrop.ui.controller.activity.retriever.ActivityRetriever
import com.autocrop.ui.controller.activity.retriever.ContextBasedActivityRetriever
import com.autocrop.ui.elements.ExtendedAppCompatImageButton
import com.autocrop.utils.android.extensions.recyclerView

class ComparisonButton(context: Context, attributeSet: AttributeSet):
    ExtendedAppCompatImageButton(context, attributeSet),
    ActivityRetriever<IODeterminationActivity> by ContextBasedActivityRetriever(context) {

    override fun onClickListener() {
        fragmentHostingActivity.fragmentReplacementTransaction(ComparisonFragment())
            .addToBackStack(null)
            .apply{
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