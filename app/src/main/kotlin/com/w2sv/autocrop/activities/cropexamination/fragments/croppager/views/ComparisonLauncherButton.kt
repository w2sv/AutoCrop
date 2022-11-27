package com.w2sv.autocrop.activities.cropexamination.fragments.croppager.views

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageButton
import androidx.fragment.app.findFragment
import com.w2sv.androidutils.extensions.ifNotInEditMode
import com.w2sv.androidutils.extensions.viewModel
import com.w2sv.autocrop.activities.FragmentHostingActivity
import com.w2sv.autocrop.activities.cropexamination.fragments.comparison.ComparisonFragment
import com.w2sv.autocrop.activities.cropexamination.fragments.croppager.CropPager
import com.w2sv.autocrop.activities.cropexamination.fragments.croppager.CropPagerFragment
import com.w2sv.autocrop.activities.cropexamination.fragments.croppager.viewmodel.CropPagerViewModel
import com.w2sv.bidirectionalviewpager.viewpager.currentViewHolder

class ComparisonLauncherButton(context: Context, attributeSet: AttributeSet) :
    AppCompatImageButton(context, attributeSet),
    FragmentHostingActivity.Retriever by FragmentHostingActivity.Retriever.Implementation(context) {

    private val viewModel by viewModel<CropPagerViewModel>()

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        ifNotInEditMode {
            setOnClickListener {
                launchComparisonFragment()
            }
        }
    }

    private fun launchComparisonFragment() {
        fragmentHostingActivity.fragmentReplacementTransaction(
            ComparisonFragment.instance(viewModel.dataSet.liveElement)
        )
            .addToBackStack(null)
            .apply {
                val cropImageView =
                    findFragment<CropPagerFragment>()
                        .binding
                        .viewPager
                        .currentViewHolder<CropPager.Adapter.ViewHolder>()!!
                        .imageView

//                cropImageView.layoutParams = (cropImageView.layoutParams as RecyclerView.LayoutParams).apply {
//                    val margin = (rootView.height - viewModel.dataSet.liveElement.crop.edges.height) / 2
//                    topMargin = margin
//                    bottomMargin = margin
//                }

                addSharedElement(
                    cropImageView,
                    cropImageView.transitionName
                )
            }
            .commit()
    }
}