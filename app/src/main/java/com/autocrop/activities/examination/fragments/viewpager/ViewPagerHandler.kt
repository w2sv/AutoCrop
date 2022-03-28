package com.autocrop.activities.examination.fragments.viewpager

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.SuppressLint
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnticipateInterpolator
import android.widget.ImageView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.autocrop.UserPreferences
import com.autocrop.activities.examination.ExaminationActivity
import com.autocrop.activities.examination.ExaminationViewModel
import com.autocrop.activities.examination.ViewPagerModel
import com.autocrop.utils.Index
import com.autocrop.utils.android.*
import com.autocrop.utils.get
import com.autocrop.utils.toInt
import com.google.android.material.snackbar.Snackbar
import com.w2sv.autocrop.R
import com.w2sv.autocrop.databinding.ActivityExaminationFragmentViewpagerBinding
import java.util.*

class ViewPagerHandler(private val binding: ActivityExaminationFragmentViewpagerBinding){

    private val examinationActivity: ExaminationActivity
        get() = binding.viewPager.context as ExaminationActivity

    private val activityViewModel: ExaminationViewModel by lazy { ViewModelProvider(examinationActivity)[ExaminationViewModel::class.java] }
    private val viewModel: ViewPagerModel
        get() = activityViewModel.viewPager

    val scroller = Scroller()
    var blockPageDependentViewUpdating = false

    init {
        binding.viewPager.apply {
            // instantiate adapter, set first crop
            adapter = CropPagerAdapter()

            setCurrentItem(
                viewModel.startPosition,
                false
            )

            if (viewModel.dataSet.size > 1)
                examinationActivity.runOnUiThread { binding.pageIndicationSeekBar.show() }

            // register onPageChangeCallbacks
            registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {

                /**
                 * Updates retentionPercentage, pageIndication and seekBar upon page change if not blocked
                 */
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)

                    if (!blockPageDependentViewUpdating)
                        binding.updatePageDependentViews(viewModel.dataSet.correspondingPosition(position))
                }
            })

            /**
             * Set Cube Out Page Transformer
             *
             * Reference: https://www.loginworks.com/blogs/how-to-make-awesome-transition-
             * effects-using-pagetransformer-in-android/
             */
            setPageTransformer { view: View, position: Float ->
                with(view) {
                    pivotX = listOf(0f, width.toFloat())[(position < 0f).toInt()]
                    pivotY = height * 0.5f
                    rotationY = 90f * position
                }
            }
        }
        // run Scroller if applicable
        if (viewModel.conductAutoScroll)
            scroller.invoke()
        else
            examinationActivity.runOnUiThread { binding.discardedTextView.show() }
    }

    private fun ActivityExaminationFragmentViewpagerBinding.updatePageDependentViews(dataSetPosition: Index) =
        examinationActivity.runOnUiThread{
            toolbar.pageIndication.updateText(dataSetPosition)
            discardedTextView.updateText(dataSetPosition)
            pageIndicationSeekBar.update(dataSetPosition)
        }

    /**
     * Class accounting for automatic scrolling at the start of crop examination
     */
    inner class Scroller {
        var isRunning: Boolean = false
        private var conductedScrolls = 0
        private val maxScrolls = viewModel.dataSet.lastIndex
        private lateinit var timer: Timer

        fun invoke(){
            isRunning = true
            examinationActivity.runOnUiThread { binding.autoScrollingTextView.show() }

            timer = Timer().apply {
                schedule(
                    object : TimerTask() {
                        override fun run() {
                            Handler(Looper.getMainLooper()).post {
                                with(binding.viewPager) { setCurrentItem(currentItem + 1, true) }
                                conductedScrolls++
                            }

                            if (conductedScrolls == maxScrolls)
                                cancel(onScreenTouch = false)
                        }
                    },
                    listOf(1000L, 1500L)[viewModel.longAutoScrollDelay],
                    1000L
                )
            }
        }

        /**
         * Cancels timer, displays Snackbar as to the cause of cancellation
         */
        fun cancel(onScreenTouch: Boolean) {
            timer.cancel()
            isRunning = false
            examinationActivity.runOnUiThread {
                val duration = examinationActivity.resources.getInteger(android.R.integer.config_longAnimTime).toLong()

                binding.autoScrollingTextView.fadeOut(duration)
                binding.discardedTextView.fadeIn(duration)
            }

            examinationActivity.displaySnackbar(
                listOf("Traversed all crops", "Cancelled auto scrolling")[onScreenTouch],
                TextColors.SUCCESS,
                Snackbar.LENGTH_SHORT
            )
        }
    }

    inner class CropPagerAdapter: RecyclerView.Adapter<CropPagerAdapter.CropViewHolder>() {

        @SuppressLint("ClickableViewAccessibility")
        inner class CropViewHolder(view: ImageView) : RecyclerView.ViewHolder(view) {
            val cropView: ImageView = view.findViewById(R.id.slide_item_image_view_examination_activity)

            /**
             * Set onTouchListener through implementation of ImmersiveViewOnTouchListener
             */
            init {
                cropView.setOnTouchListener(
                    object : ImmersiveViewOnTouchListener() {

                        /**
                         * Cancel scroller upon touch if running
                         */
                        override fun onTouch(v: View?, event: MotionEvent?): Boolean = scroller.run {
                            if (isRunning)
                                false
                                    .also { cancel(true) }
                            else
                                super.onTouch(v, event)
                        }

                        /**
                         * Invoke CropProcedureDialog upon click
                         */
                        override fun onClick() {
                            viewModel.dataSet.correspondingPosition(adapterPosition).let{ dataSetPosition ->
                                CropProcedureDialog(
                                    viewModel.dataSet[dataSetPosition].screenshotUri to viewModel.dataSet[dataSetPosition].crop,
                                    imageFileWritingContext = examinationActivity)
                                {incrementNSavedCrops -> onCropProcedureAction(dataSetPosition, incrementNSavedCrops)}
                                    .show(examinationActivity.supportFragmentManager, "CROP_PROCEDURE_DIALOG")
                            }
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
                    .inflate(R.layout.slide_item_container_examination_activity, parent, false)
                        as ImageView
            )
        }

        /**
         * Defines crop setting wrt [position]
         */
        override fun onBindViewHolder(holder: CropViewHolder, position: Index) {
            holder.cropView.setImageBitmap(viewModel.dataSet.atCorrespondingPosition(position).crop)
        }

        override fun getItemCount(): Int = listOf(1, ViewPagerModel.MAX_VIEWS)[viewModel.dataSet.size > 1]

        /**
         * Increment nSavedCrops if applicable & triggers activity exit if cropBundleList
         * about to be exhausted; otherwise removes current view, the procedure action has been
         * selected for, from pager
         */
        private fun onCropProcedureAction(dataSetPosition: Index, incrementNSavedCrops: Boolean){
            if (incrementNSavedCrops){
                activityViewModel.nSavedCrops += 1
                if (UserPreferences.deleteIndividualScreenshot)
                    activityViewModel.nDeletedCrops += 1
            }
            if (viewModel.dataSet.size == 1)
                return examinationActivity.run { replaceCurrentFragmentWith(appTitleFragment, true) }
            else if (viewModel.dataSet.size == 2)
                examinationActivity.runOnUiThread {
                    binding.pageIndicationSeekBar.animate()
                        .scaleX(0f)
                        .scaleY(0f)
                        .setInterpolator(AnticipateInterpolator())
                        .setListener(object : AnimatorListenerAdapter() {
                            override fun onAnimationEnd(animation: Animator) {
                                binding.pageIndicationSeekBar.remove()
                            }
                        })
                        .duration = 700L
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
            val (newDataSetPosition, newViewPosition) = viewModel.dataSet.newPositionWithNewViewPosition(dataSetPosition, binding.viewPager.currentItem)

            // scroll to newViewPosition with blocked pageDependentViewUpdating
            blockPageDependentViewUpdating = true
            binding.viewPager.setCurrentItem(newViewPosition, true)
            blockPageDependentViewUpdating = false

            // remove cropBundle from dataSet, rotate dataSet and reset position trackers such that
            // aligning with newViewPosition
            viewModel.dataSet.removeAt(dataSetPosition)
            viewModel.dataSet.rotateAndResetPositionTrackers(newViewPosition, newDataSetPosition)

            // update surrounding views
            resetViewsAround(newViewPosition)

            // update views
            binding.updatePageDependentViews(viewModel.dataSet.correspondingPosition(newViewPosition))
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