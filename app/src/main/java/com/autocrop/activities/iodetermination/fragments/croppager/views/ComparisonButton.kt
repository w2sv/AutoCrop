package com.autocrop.activities.iodetermination.fragments.croppager.views

import android.content.Context
import android.util.AttributeSet
import android.view.View
import com.autocrop.activities.iodetermination.IODeterminationActivity
import com.autocrop.activities.iodetermination.fragments.comparison.ComparisonFragment
import com.autocrop.activities.iodetermination.fragments.croppager.CropPagerFragment
import com.autocrop.activities.iodetermination.fragments.croppager.pager.CropPagerAdapter
import com.autocrop.activities.iodetermination.fragments.croppager.viewmodel.CropPagerViewModel
import com.autocrop.controller.activity.retriever.ActivityRetriever
import com.autocrop.controller.activity.retriever.ContextBasedActivityRetriever
import com.autocrop.utils.android.extensions.viewModelLazy
import com.autocrop.ui.views.ExtendedAppCompatImageButton
import com.autocrop.utils.android.extensions.recyclerView
import com.autocrop.utils.android.extensions.show
import com.autocrop.utils.android.extensions.snackyBuilder

class ComparisonButton(context: Context, attributeSet: AttributeSet) :
    ExtendedAppCompatImageButton(context, attributeSet),
    ActivityRetriever<IODeterminationActivity> by ContextBasedActivityRetriever(context) {

    private val viewModel by viewModelLazy<CropPagerViewModel>()

    override fun onClickListener() {
        fragmentHostingActivity.fragmentReplacementTransaction(
            ComparisonFragment.instance(viewModel.dataSet.currentElement)
        )
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