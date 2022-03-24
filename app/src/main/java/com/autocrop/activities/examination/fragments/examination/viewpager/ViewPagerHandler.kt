package com.autocrop.activities.examination.fragments.examination.viewpager

import android.annotation.SuppressLint
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.autocrop.activities.examination.ExaminationActivity
import com.autocrop.activities.examination.ExaminationViewModel
import com.autocrop.activities.examination.ViewPagerModel
import com.autocrop.crop
import com.autocrop.cropBundleList
import com.autocrop.screenshotUri
import com.autocrop.utils.Index
import com.autocrop.utils.android.TextColors
import com.autocrop.utils.android.displaySnackbar
import com.autocrop.utils.get
import com.autocrop.utils.toInt
import com.google.android.material.snackbar.Snackbar
import com.w2sv.autocrop.R
import com.w2sv.autocrop.databinding.ActivityExaminationFragmentRootBinding
import java.util.*

class ViewPagerHandler(private val binding: ActivityExaminationFragmentRootBinding){

    private val parentActivity: ExaminationActivity
        get() = binding.viewPager.context as ExaminationActivity

    private val viewModel: ExaminationViewModel by lazy {
        ViewModelProvider(parentActivity)[ExaminationViewModel::class.java]
    }

    val scroller: Scroller
    var blockPageDependentViewUpdating = false

    init {
        // instantiate adapter, display first crop
        binding.viewPager.apply {
            adapter = CropPagerAdapter()

            setCurrentItem(
                viewModel.viewPager.startPosition,
                false
            )

            // register onPageChangeCallbacks
            registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {

                /**
                 * Updates retentionPercentage, pageIndication and seekBar upon page change if not blocked
                 */
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)

                    if (!blockPageDependentViewUpdating)
                        binding.updatePageDependentViews(viewModel.viewPager.dataSet.correspondingPosition(position))
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

            // instantiate Scroller if applicable
            scroller = Scroller(viewModel.viewPager.longAutoScrollDelay, viewModel.viewPager.dataSet.lastIndex).apply {
                if (viewModel.viewPager.conductAutoScroll)
                    invoke()
            }
        }
    }

    private fun ActivityExaminationFragmentRootBinding.updatePageDependentViews(dataSetPosition: Index){
        toolbar.retentionPercentage.updateText(dataSetPosition)
        toolbar.pageIndication.updateText(dataSetPosition)
        pageIndicationSeekBar.update(dataSetPosition)
    }

    /**
     * Class accounting for automatic scrolling at the start of crop examination
     */
    inner class Scroller(private val longAutoScrollDelay: Boolean, private val maxScrolls: Int) {
        var isRunning: Boolean = false
        private var conductedScrolls = 0
        private lateinit var timer: Timer

        fun invoke(){
            isRunning = true
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
                    listOf(1000L, 1500L)[longAutoScrollDelay],
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

            parentActivity.displaySnackbar(
                listOf("Traversed all crops", "Cancelled auto scrolling")[onScreenTouch],
                TextColors.successfullyCarriedOut,
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
                            with(viewModel.viewPager.dataSet.correspondingPosition(adapterPosition)){
                                CropProcedureDialog(
                                    Pair(viewModel.viewPager.dataSet[this].screenshotUri, viewModel.viewPager.dataSet[this].crop),
                                    imageFileWritingContext = parentActivity
                                ) {incrementNSavedCrops -> onCropProcedureAction(this, incrementNSavedCrops)}
                                    .show(parentActivity.supportFragmentManager, CropProcedureDialog.TAG)
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
         * Defines setting of crop wrt position
         */
        override fun onBindViewHolder(holder: CropViewHolder, position: Index) {
            holder.cropView.setImageBitmap(viewModel.viewPager.dataSet.atCorrespondingPosition(position).crop)
        }

        override fun getItemCount(): Int = listOf(1, ViewPagerModel.MAX_VIEWS)[viewModel.viewPager.dataSet.size > 1]

        /**
         * Increment nSavedCrops if applicable, triggers activity exit if cropBundleList
         * about to be exhausted
         */
        private fun onCropProcedureAction(dataSetPosition: Index, incrementNSavedCrops: Boolean){
            if (incrementNSavedCrops)
                viewModel.incrementNSavedCrops()
            if (viewModel.viewPager.dataSet.size == 1)
                return parentActivity.run { appTitleFragment.commit(true) }

            removeView(dataSetPosition)
        }

        private fun removeView(dataSetPosition: Index) {
            val (newDataSetPosition, newViewPosition) = viewModel.viewPager.dataSet.newPositionWithNewViewPosition(dataSetPosition, binding.viewPager.currentItem)

            // scroll to newViewPosition with blocked pageDependentViewUpdating
            blockPageDependentViewUpdating = true
            binding.viewPager.setCurrentItem(newViewPosition, true)
            blockPageDependentViewUpdating = false

            // remove cropBundle from dataSet, rotate dataSet and reset position trackers such that
            // aligning with newViewPosition
            viewModel.viewPager.dataSet.removeAt(dataSetPosition)
            viewModel.viewPager.dataSet.rotateAndResetPositionTrackers(newViewPosition, newDataSetPosition)

            // update surrounding views
            updateViewsAround(newViewPosition)

            // update views
            binding.updatePageDependentViews(newDataSetPosition)
        }

        private fun updateViewsAround(position: Index){
            val resetMargin = 3

            notifyItemRangeChanged(position - resetMargin, resetMargin * 2 + 1)
        }
    }
}