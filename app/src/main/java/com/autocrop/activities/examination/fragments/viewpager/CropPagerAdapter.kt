package com.autocrop.activities.examination.fragments.viewpager

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.autocrop.activities.examination.fragments.viewpager.dialogs.SingleCropProcedureDialog
import com.autocrop.utils.BlankFun
import com.autocrop.utils.Index
import com.w2sv.autocrop.R

class CropPagerAdapter(
    private val viewPager2: ViewPager2,
    private val viewModel: ViewPagerViewModel,
    private val lastCropProcessedListener: BlankFun):
        RecyclerView.Adapter<CropPagerAdapter.CropViewHolder>() {

    val cropProcedureDialog: SingleCropProcedureDialog by lazy{
        SingleCropProcedureDialog()
    }
    val fragmentActivity: FragmentActivity by lazy{
        viewPager2.context as FragmentActivity
    }

    @SuppressLint("ClickableViewAccessibility")
    inner class CropViewHolder(view: ImageView)
        : RecyclerView.ViewHolder(view) {

        val cropView: ImageView = view.findViewById(R.id.image_view_examination_view_pager)

        /**
         * Set onTouchListener through implementation of ImmersiveViewOnTouchListener
         */
        init {
            cropView.setOnTouchListener(
                object : ImmersiveViewOnTouchListener() {

                    init {
                        fragmentActivity.supportFragmentManager.setFragmentResultListener(
                            SingleCropProcedureDialog.PROCEDURE_SELECTED, fragmentActivity){ _, bundle ->
                            onCropProcedureSelected(
                                bundle.getInt(SingleCropProcedureDialog.DATA_SET_POSITION_OUT)
                            )
                        }
                    }

                    /**
                     * Cancel scroller upon touch if running
                     */
                    override fun onTouch(v: View?, event: MotionEvent?): Boolean =
                        if (viewModel.autoScroll.value!!) {
                            viewModel.autoScroll.postValue(false)
                            false
                        }
                        else
                            super.onTouch(v, event)

                    /**
                     * Invoke CropProcedureDialog
                     */
                    override fun onClick() = with(cropProcedureDialog) {
                        arguments = bundleOf(
                            SingleCropProcedureDialog.DATA_SET_POSITION_IN to viewModel.dataSet.correspondingPosition(
                                adapterPosition
                            )
                        )
                        show(fragmentActivity.supportFragmentManager)
                    }
                }
            )
        }
    }

    /**
     * Defines creation of CropViewHolder
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CropViewHolder =
        CropViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.examination_view_pager_image_view, parent, false)
                    as ImageView
        )

    /**
     * Defines crop setting wrt [position]
     */
    override fun onBindViewHolder(holder: CropViewHolder, position: Index) =
        holder.cropView.setImageBitmap(viewModel.dataSet.atCorrespondingPosition(position).crop)

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
        if (viewModel.dataSet.size == 1)
            cropProcedureDialog.processingJob?.run {
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