package com.autocrop.activities.examination.fragments.viewpager.views

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.PopupMenu
import com.autocrop.activities.examination.ExaminationActivity
import com.autocrop.activities.examination.fragments.comparison.ComparisonFragment
import com.autocrop.activities.examination.fragments.viewpager.CropPagerAdapter
import com.autocrop.activities.examination.fragments.viewpager.ViewPagerFragment
import com.autocrop.activities.examination.fragments.viewpager.ViewPagerViewModel
import com.autocrop.uielements.AbstractMenuInflationButton
import com.autocrop.retriever.activity.ActivityRetriever
import com.autocrop.retriever.activity.ContextBasedActivityRetriever
import com.autocrop.retriever.viewmodel.ViewModelRetriever
import com.autocrop.uielements.ExtendedPopupMenu
import com.autocrop.uielements.recyclerView
import com.lyrebirdstudio.croppylib.Croppy
import com.lyrebirdstudio.croppylib.main.CropRequest
import com.w2sv.autocrop.R

class MenuInflationButton(context: Context, attributeSet: AttributeSet) :
    AbstractMenuInflationButton(context, attributeSet) {

    companion object {
        const val MANUAL_CROP_REQUEST_CODE = 69
    }

    override fun instantiatePopupMenu(): PopupMenu =
        object :
            ExtendedPopupMenu(context, this, R.menu.fragment_viewpager),
            ActivityRetriever<ExaminationActivity> by ContextBasedActivityRetriever(context),
            ViewModelRetriever<ViewPagerViewModel> by ViewPagerViewModelRetriever(context) {

            init {
                setItemOnClickListeners(
                    mapOf(
                        R.id.menu_item_compare to this::launchComparisonFragment,
                        R.id.menu_item_adjust_crop to this::launchManualCroppingActivity
                    )
                )
            }

            private fun launchManualCroppingActivity() {
                Croppy.start(
                    activity,
                    CropRequest.Auto(
                        sharedViewModel.dataSet.currentCropBundle.screenshot.uri,
                        requestCode = MANUAL_CROP_REQUEST_CODE
                    )
                )
            }

            private fun launchComparisonFragment() {
                typedActivity.replaceCurrentFragmentWith(
                    ComparisonFragment(),
                    addToBackStack = true
                ) { fragmentTransaction ->
                    val cropImageView =
                        typedActivity.castCurrentFragment<ViewPagerFragment>().binding.viewPager.run {
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