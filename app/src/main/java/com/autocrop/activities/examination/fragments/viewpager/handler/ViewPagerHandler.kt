package com.autocrop.activities.examination.fragments.viewpager.handler

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.os.bundleOf
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.autocrop.activities.examination.ExaminationActivity
import com.autocrop.activities.examination.fragments.viewpager.ViewPagerFragmentViewModel
import com.autocrop.activities.examination.fragments.viewpager.dialogs.SingleCropProcedureDialog
import com.autocrop.uielements.CubeOutPageTransformer
import com.autocrop.uielements.view.crossFade
import com.autocrop.uielements.view.show
import com.autocrop.uielements.view.shrinkAndFinallyRemove
import com.autocrop.utils.Index
import com.autocrop.utils.android.*
import com.w2sv.autocrop.R
import com.w2sv.autocrop.databinding.ActivityExaminationFragmentViewpagerBinding

class ViewPagerHandler(
    private val binding: ActivityExaminationFragmentViewpagerBinding,
    private val viewModel: ViewPagerFragmentViewModel,
    private val examinationActivity: ExaminationActivity,
    previousPosition: Int?){

    private val pageChangeHandler = PageChangeHandler(viewModel.startPosition) { position, onRightScroll ->
        binding.updatePageDependentViews(viewModel.dataSet.correspondingPosition(position), onRightScroll)
    }
    private val scroller: Scroller = Scroller { onScreenTouch ->
        viewModel.autoScroll = false

        examinationActivity.runOnUiThread {
            crossFade(
                examinationActivity.resources.getInteger(R.integer.visibility_changing_animation_duration).toLong(),
                binding.autoScrollingTextView,
                binding.discardingStatisticsTv, binding.toolbar
            )
        }

        if (onScreenTouch){
            binding.viewPager.setPageTransformer()
            examinationActivity.displaySnackbar(
                "Cancelled auto scrolling",
                R.drawable.ic_outline_cancel_24
            )
        }
        else
            pageChangeHandler.onNextScrollCompletion = { binding.viewPager.setPageTransformer() }
    }

    init {
        binding.viewPager.initialize(previousPosition)
        binding.initializeViews()
    }

    private fun ViewPager2.initialize(previousPosition: Int?){
        // set adapter + first view
        adapter = CropPagerAdapter()

        setCurrentItem(
            previousPosition ?: viewModel.startPosition,
            false
        )

        // register onPageChangeCallbacks
        registerOnPageChangeCallback(pageChangeHandler)
    }

    private fun ActivityExaminationFragmentViewpagerBinding.initializeViews(){
        val dataSetPosition = viewModel.dataSet.correspondingPosition(viewPager.currentItem)
        updatePageDependentViews(dataSetPosition)

        // display pageIndicationElements if applicable
        if (viewModel.dataSet.size > 1)
            examinationActivity.runOnUiThread {
                pageIndicationElements.show()
            }

        // run Scroller and display respective text view if applicable;
        // otherwise display discardedTextView and set page transformer
        if (viewModel.autoScroll)
            scroller.run(viewPager, viewModel.dataSet.size - dataSetPosition, autoScrollingTextView)
        else{
            examinationActivity.runOnUiThread {
                discardingStatisticsTv.show()
                toolbar.show()
            }
            viewPager.setPageTransformer()
        }
    }

    private fun ActivityExaminationFragmentViewpagerBinding.updatePageDependentViews(dataSetPosition: Index, onRightScroll: Boolean = false) =
        examinationActivity.runOnUiThread{
            discardingStatisticsTv.updateText(dataSetPosition)

            viewModel.dataSet.pageIndex(dataSetPosition).let{ pageIndex ->
                pageIndicationTv.updateText(pageIndex)
                pageIndicationSeekBar.update(pageIndex, onRightScroll)
            }
        }

    private fun ViewPager2.setPageTransformer() =
        setPageTransformer(CubeOutPageTransformer())

    private inner class CropPagerAdapter: RecyclerView.Adapter<CropPagerAdapter.CropViewHolder>() {

        val cropProcedureDialog = SingleCropProcedureDialog()

        @SuppressLint("ClickableViewAccessibility")
        private inner class CropViewHolder(view: ImageView) : RecyclerView.ViewHolder(view) {
            val cropView: ImageView = view.findViewById(R.id.image_view_examination_view_pager)

            /**
             * Set onTouchListener through implementation of ImmersiveViewOnTouchListener
             */
            init {
                cropView.setOnTouchListener(
                    object : ImmersiveViewOnTouchListener() {

                        /**
                         * Cancel scroller upon touch if running
                         */
                        override fun onTouch(v: View?, event: MotionEvent?): Boolean =
                            if (viewModel.autoScroll) {
                                scroller.cancel(true)
                                false
                            }
                            else
                                super.onTouch(v, event)

                        init {
                            examinationActivity.supportFragmentManager.setFragmentResultListener(
                                SingleCropProcedureDialog.PROCEDURE_SELECTED, examinationActivity){
                                    _, bundle -> onCropProcedureSelected(bundle.getInt(
                                SingleCropProcedureDialog.DATA_SET_POSITION_OUT))
                            }
                        }

                        /**
                         * Invoke CropProcedureDialog
                         */
                        override fun onClick() = with(cropProcedureDialog) {
                            arguments = bundleOf(
                                SingleCropProcedureDialog.DATA_SET_POSITION_IN to viewModel.dataSet.correspondingPosition(adapterPosition)
                            )
                            show(examinationActivity.supportFragmentManager)
                        }
                    }
                )
            }
        }

        /**
         * Defines creation of CropViewHolder
         */
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CropViewHolder {
            return CropViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.activity_examination_view_pager_image_view, parent, false)
                        as ImageView
            )
        }

        /**
         * Defines crop setting wrt [position]
         */
        override fun onBindViewHolder(holder: CropViewHolder, position: Index) =
            holder.cropView.setImageBitmap(viewModel.dataSet.atCorrespondingPosition(position).crop)

        override fun getItemCount(): Int =
            if (viewModel.dataSet.size > 1) ViewPagerFragmentViewModel.MAX_VIEWS else 1

        /**
         * Increment nSavedCrops if applicable
         *
         * triggers activity exit if [viewModel].dataSet about to be exhausted OR
         * hide [binding].pageIndicationSeekBar AND/OR
         * removes view, procedure action has been selected for, from pager
         */
        private fun onCropProcedureSelected(dataSetPosition: Index){
            when(viewModel.dataSet.size){
                1 -> {
                    (binding.viewPager.adapter as CropPagerAdapter).cropProcedureDialog.cropBundleProcessingJob?.run {
                        invokeOnCompletion {
                            examinationActivity.invokeSubsequentFragment()
                        }
                    } ?: examinationActivity.invokeSubsequentFragment()
                    return
                }
                2 -> examinationActivity.runOnUiThread { binding.pageIndicationElements.shrinkAndFinallyRemove() }
                else -> Unit
            }
            removeView(dataSetPosition)
        }

        /**
         * • scroll to subsequent position
         * • remove cropBundle from dataSet
         * • rotate dataSet such that it will subsequently align with the determined newViewPosition again
         * • reset preloaded views around newViewPosition
         * • update page dependent views
         */
        private fun removeView(dataSetPosition: Index) {
            val removingAtDataSetTail = viewModel.dataSet.removingAtTail(dataSetPosition)
            val newViewPosition = binding.viewPager.currentItem + viewModel.dataSet.viewPositionIncrement(removingAtDataSetTail)

            // scroll to newViewPosition with blocked pageDependentViewUpdating
            pageChangeHandler.updateViews = false
            binding.viewPager.setCurrentItem(newViewPosition, true)
            pageChangeHandler.updateViews = true

            pageChangeHandler.removeView = {
                // remove cropBundle from dataSet, rotate dataSet and reset position trackers such that
                // aligning with newViewPosition
                viewModel.dataSet.removeAtAndRealign(dataSetPosition, removingAtDataSetTail, newViewPosition)

                // update surrounding views
                resetViewsAround(newViewPosition)

                // update views
                binding.updatePageDependentViews(viewModel.dataSet.correspondingPosition(newViewPosition))
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
}