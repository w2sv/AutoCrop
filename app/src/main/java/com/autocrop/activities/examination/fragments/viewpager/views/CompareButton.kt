package com.autocrop.activities.examination.fragments.viewpager.views

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import androidx.appcompat.widget.AppCompatButton
import androidx.recyclerview.widget.RecyclerView
import com.autocrop.activities.examination.ExaminationActivity
import com.autocrop.activities.examination.fragments.comparison.ComparisonFragment
import com.autocrop.activities.examination.fragments.viewpager.CropPagerAdapter
import com.autocrop.activities.examination.fragments.viewpager.ViewPagerFragment
import com.autocrop.activities.examination.fragments.viewpager.ViewPagerViewModel
import com.autocrop.uicontroller.ViewModelRetriever
import com.autocrop.uielements.recyclerView
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