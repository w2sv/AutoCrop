package com.autocrop.activities.examination.fragments.croppager.views

import android.content.Context
import android.util.AttributeSet
import com.autocrop.activities.examination.ExaminationActivity
import com.autocrop.activities.examination.fragments.comparison.ComparisonFragment
import com.autocrop.activities.examination.fragments.croppager.CropPagerFragment
import com.autocrop.activities.examination.fragments.croppager.pager.CropPagerAdapter
import com.autocrop.retriever.activity.ActivityRetriever
import com.autocrop.retriever.activity.ContextBasedActivityRetriever
import com.autocrop.ui.elements.ExtendedAppCompatImageButton
import com.autocrop.ui.elements.recyclerview.recyclerView

class ComparisonButton(context: Context, attributeSet: AttributeSet):
    ExtendedAppCompatImageButton(context, attributeSet),
    ActivityRetriever<ExaminationActivity> by ContextBasedActivityRetriever(context){

    override fun onClickListener() {
        fragmentHostingActivity.replaceCurrentFragmentWith(
            ComparisonFragment(),
            addToBackStack = true
        ) { fragmentTransaction ->
            val cropImageView =
                fragmentHostingActivity.castCurrentFragment<CropPagerFragment>().binding.viewPager.run {
                    (recyclerView.findViewHolderForAdapterPosition(currentItem) as CropPagerAdapter.CropViewHolder).cropImageView
                }

            fragmentTransaction.addSharedElement(
                cropImageView,
                cropImageView.transitionName
            )
        }
    }
}