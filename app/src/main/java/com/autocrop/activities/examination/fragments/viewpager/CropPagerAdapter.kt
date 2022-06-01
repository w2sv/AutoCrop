package com.autocrop.activities.examination.fragments.viewpager

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.autocrop.activities.examination.ExaminationActivity
import com.autocrop.activities.examination.ExaminationActivityViewModel
import com.autocrop.activities.examination.ExaminationActivityViewModelRetriever
import com.autocrop.activities.examination.fragments.viewpager.dialogs.SingleCropProcedureDialog
import com.autocrop.collections.CropBundle
import com.autocrop.retriever.viewmodel.ViewModelRetriever
import com.autocrop.retriever.activity.ActivityRetriever
import com.autocrop.retriever.activity.ContextBasedActivityRetriever
import com.autocrop.utils.BlankFun
import com.autocrop.utils.Index
import com.w2sv.autocrop.R

fun CropBundle.transitionName(): String = hashCode().toString()

class CropPagerAdapter(
    private val viewPager2: ViewPager2,
    private val viewModel: ViewPagerViewModel,
    private val lastCropProcessedListener: BlankFun):
        RecyclerView.Adapter<CropPagerAdapter.CropViewHolder>(),
        ViewModelRetriever<ExaminationActivityViewModel> by ExaminationActivityViewModelRetriever(viewPager2.context),
        ActivityRetriever<ExaminationActivity> by ContextBasedActivityRetriever(viewPager2.context) {

    init {
        fragmentActivity.supportFragmentManager.setFragmentResultListener(
            SingleCropProcedureDialog.PROCEDURE_SELECTED,
            fragmentActivity
        ){ _, bundle ->
            onCropProcedureSelected(
                bundle.getInt(SingleCropProcedureDialog.DATA_SET_POSITION_OUT)
            )
        }
    }

    class CropViewHolder(view: ImageView)
        : RecyclerView.ViewHolder(view) {

        val cropImageView: ImageView = view.findViewById(R.id.crop_iv)
    }

    /**
     * Defines creation of CropViewHolder
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CropViewHolder =
        CropViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.examination_crop_view_holder, parent, false) as ImageView
        )

    /**
     * Defines crop setting wrt [position]
     */
    override fun onBindViewHolder(holder: CropViewHolder, position: Index){
        holder.cropImageView.apply{
            val cropBundle = viewModel.dataSet.atCorrespondingPosition(position)

            setImageBitmap(cropBundle.crop.bitmap)
            ViewCompat.setTransitionName(this, cropBundle.transitionName())
        }
    }

    override fun getItemCount(): Int =
        if (viewModel.dataSet.size > 1) ViewPagerViewModel.MAX_VIEWS else 1

    /**
     * Increment nSavedCrops if applicable
     *
     * triggers activity exit if [viewModel].dataSet about to be exhausted OR
     * hide pageIndicationSeekBar AND/OR
     * removes view, procedure action has been selected for, from pager
     */
    private fun onCropProcedureSelected(dataSetPosition: Index) =
        if (viewModel.dataSet.containsSingleElement)
            sharedViewModel.singleCropSavingJob?.run{
                invokeOnCompletion {
                    lastCropProcessedListener()
                }
            } ?: lastCropProcessedListener()
        else
            removeView(dataSetPosition)

    /**
     * • scroll to subsequent position
     * • remove cropBundle from dataSet
     * • rotate dataSet such that it will subsequently align with the determined newViewPosition again
     * • reset preloaded views around newViewPosition
     * • update page dependent views
     */
    private fun removeView(dataSetPosition: Index) {
        val removingAtDataSetTail = viewModel.dataSet.removingAtTail(dataSetPosition)
        val newViewPosition = viewPager2.currentItem + viewModel.dataSet.viewPositionIncrement(removingAtDataSetTail)

        // scroll to newViewPosition with blocked pageDependentViewUpdating
        viewModel.blockSubsequentPageRelatedViewsUpdate()
        viewPager2.setCurrentItem(newViewPosition, true)

        viewModel.scrollStateIdleListenerConsumable = {
            // remove cropBundle from dataSet, rotate dataSet and reset position trackers such that
            // aligning with newViewPosition
            viewModel.dataSet.removeAtAndRealign(dataSetPosition, removingAtDataSetTail, newViewPosition)

            // update surrounding views
            resetViewsAround(newViewPosition)

            // update views
            viewModel.setDataSetPosition(newViewPosition)
        }
    }

    /**
     * Triggers reloading of preloaded views surrounding the one sitting at [position]
     *
     * Recycler View preloads and cashes 3 views to either side of the one being currently displayed
     */
    private fun resetViewsAround(position: Index){
        val nPreloadedViewsToEitherSide = 3
        
        notifyItemRangeChanged(position - nPreloadedViewsToEitherSide, nPreloadedViewsToEitherSide * 2 + 1)
    }
}