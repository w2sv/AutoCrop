package com.autocrop.activities.examination.fragments.croppager.views

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.PopupMenu
import androidx.core.graphics.toRectF
import com.autocrop.activities.examination.ExaminationActivity
import com.autocrop.activities.examination.fragments.comparison.ComparisonFragment
import com.autocrop.activities.examination.fragments.croppager.pager.CropPagerAdapter
import com.autocrop.activities.examination.fragments.croppager.CropPagerFragment
import com.autocrop.activities.examination.fragments.croppager.viewmodel.ViewPagerViewModel
import com.autocrop.retriever.activity.ActivityRetriever
import com.autocrop.retriever.activity.ContextBasedActivityRetriever
import com.autocrop.retriever.viewmodel.ViewModelRetriever
import com.autocrop.ui.elements.menu.AbstractMenuInflationButton
import com.autocrop.ui.elements.menu.ExtendedPopupMenu
import com.autocrop.ui.elements.recyclerview.recyclerView
import com.blogspot.atifsoftwares.animatoolib.Animatoo
import com.lyrebirdstudio.croppylib.CropRequest
import com.lyrebirdstudio.croppylib.CroppyTheme
import com.lyrebirdstudio.croppylib.launchCroppyActivity
import com.w2sv.autocrop.R

class MenuInflationButton(context: Context, attributeSet: AttributeSet) :
    AbstractMenuInflationButton(context, attributeSet),
    ViewModelRetriever<ViewPagerViewModel> by ViewPagerViewModelRetriever(context){

    companion object {
        const val CROPPY_ACTIVITY_REQUEST_CODE = 69
    }

    override fun instantiatePopupMenu(): PopupMenu =
        object :
            ExtendedPopupMenu(context, this, R.menu.fragment_viewpager),
            ActivityRetriever<ExaminationActivity> by ContextBasedActivityRetriever(context),
            ViewModelRetriever<ViewPagerViewModel> by ViewPagerViewModelRetriever(context) {

            init {
                setItemOnClickListeners(
                    mapOf(
                        R.id.menu_item_compare to ::launchComparisonFragment,
                        R.id.menu_item_adjust_crop to this::launchCroppyActivity
                    )
                )
                setIcons(context, R.color.magenta_bright)
            }

            private fun launchComparisonFragment() {
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

            private fun launchCroppyActivity() {
                val transitionAnimation = Animatoo::animateInAndOut
                val cropBundle = sharedViewModel.dataSet.currentValue

                activity.launchCroppyActivity(
                    CropRequest(
                        cropBundle.screenshot.uri,
                        requestCode = CROPPY_ACTIVITY_REQUEST_CODE,
                        initialCropRect = cropBundle.crop.rect.toRectF(),
                        croppyTheme = CroppyTheme(
                            accentColor = R.color.magenta_bright,
                            backgroundColor = R.color.magenta_dark
                        ),
                        exitActivityAnimation = transitionAnimation
                    )
                )
                transitionAnimation(context)
            }
        }
}