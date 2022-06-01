package com.autocrop.activities.examination.fragments.viewpager.views

import android.content.Context
import android.util.AttributeSet
import android.widget.ImageView
import androidx.appcompat.widget.AppCompatButton
import com.autocrop.activities.examination.ExaminationActivity
import com.autocrop.activities.examination.fragments.comparison.ComparisonFragment
import com.autocrop.activities.examination.fragments.viewpager.CropPagerAdapter
import com.autocrop.activities.examination.fragments.viewpager.ViewPagerFragment
import com.autocrop.retriever.activity.ActivityRetriever
import com.autocrop.retriever.activity.ContextBasedActivityRetriever
import com.autocrop.uielements.recyclerView
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
                val cropImageView = (typedActivity.currentFragment() as ViewPagerFragment).binding.viewPager.run {
                    (recyclerView.findViewHolderForAdapterPosition(currentItem) as CropPagerAdapter.CropViewHolder).cropImageView
                }

                fragmentTransaction.addSharedElement(
                    cropImageView,
                    cropImageView.transitionName
                )
            }
        }
    }
}