package com.autocrop.activities.examination.fragments.examination.viewpager

import android.annotation.SuppressLint
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.autocrop.activities.examination.ExaminationActivity
import com.autocrop.activities.examination.ExaminationViewModel
import com.autocrop.activities.examination.fragments.examination.ExaminationFragment
import com.autocrop.activities.examination.fragments.examination.PageIndicationSeekBar
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
import java.util.*

class ViewPagerHandler(
    private val viewPager2: ViewPager2,
    private val parentActivity: ExaminationActivity,
    private val viewModel: ExaminationViewModel,
    private val textViews: ExaminationFragment.TextViews,
    private val seekBar: PageIndicationSeekBar,
    private val invokeAppTitleFragment: () -> Unit){

    var scroller: Scroller? = null

    init {
        seekBar.calculateProgressCoefficient()

        // instantiate adapter, display first crop
        viewPager2.apply {
            adapter = CropPagerAdapter()

            setCurrentItem(
                viewModel.startPosition,
                false
            )

            // register onPageChangeCallbacks
            registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {

                /**
                 * Updates retentionPercentage, pageIndication and seekBar upon page change
                 */
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)

                    if (!viewModel.cropBundleList.itemToBeRemoved)
                        with(viewModel.cropBundleList.correspondingPosition(position)) {
                            textViews.setRetentionPercentage(this)

                            with(viewModel.cropBundleList.pageIndex(this)) {
                                textViews.setPageIndication(this)
                                seekBar.indicatePage(this)
                            }
                        }
                }

                /**
                 * Removes cropBundle from cropBundleList and resets surrounding views within adapter
                 * if applicable
                 */
                override fun onPageScrollStateChanged(state: Int) {
                    super.onPageScrollStateChanged(state)

                    if (state == ViewPager2.SCROLL_STATE_IDLE && viewModel.cropBundleList.itemToBeRemoved) {
                        viewModel.cropBundleList.removeElement()

                        // reset surrounding pages
                        with(viewModel.cropBundleList.replacementViewPosition) {
                            val resetMargin = 3

                            (minus(resetMargin) until this) + (plus(1)..plus(resetMargin))
                                .forEach { (adapter as CropPagerAdapter).notifyItemChanged(it) }
                        }
                    }
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
            if (viewModel.conductAutoScroll)
                scroller = Scroller(
                    viewModel.longAutoScrollDelay,
                    cropBundleList.lastIndex
                )
        }
    }

    /**
     * Class accounting for automatic scrolling at the start of crop examination
     */
    inner class Scroller(longAutoScrollDelay: Boolean, maxScrolls: Int) {
        var isRunning: Boolean = true
        private var conductedScrolls = 0
        private var timer: Timer = Timer().apply {
            schedule(
                object : TimerTask() {
                    override fun run() {
                        Handler(Looper.getMainLooper()).post {
                            with(viewPager2) { setCurrentItem(currentItem + 1, true) }
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
                        override fun onTouch(v: View?, event: MotionEvent?): Boolean = scroller?.run {
                            if (isRunning){ cancel(true)}
                            false
                        } ?: super.onTouch(v, event)

                        /**
                         * Invoke CropProcedureDialog upon click
                         */
                        override fun onClick() {
                            with(viewModel.cropBundleList.correspondingPosition(adapterPosition)){
                                CropProcedureDialog(
                                    Pair(viewModel.cropBundleList[this].screenshotUri, viewModel.cropBundleList[this].crop),
                                    viewPager2.context
                                ) {incrementNSavedCrops -> onRemoveView(this, incrementNSavedCrops)}
                                    .show(parentActivity.supportFragmentManager, "Crop procedure dialog")
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
            holder.cropView.setImageBitmap(viewModel.cropBundleList.atCorrespondingPosition(position).crop)
        }

        override fun getItemCount(): Int = listOf(1, ExaminationViewModel.MAX_VIEWS)[viewModel.cropBundleList.size > 1]

        private fun onRemoveView(cropBundleListPosition: Index, incrementNSavedCrops: Boolean) {

            // increment nSavedCrops if applicable, triggers activity exit if cropBundleList exhausted
            if (incrementNSavedCrops)
                viewModel.incrementNSavedCrops()
            if (viewModel.cropBundleList.size == 1)
                return invokeAppTitleFragment()

            viewModel.cropBundleList.removePosition = cropBundleListPosition

            val replacementItemPositionPostRemoval = viewModel.cropBundleList.replacementItemPositionPostRemoval(viewPager2.currentItem)

            // scroll to new view and set cropBundleList rotationDistance
            viewPager2.setCurrentItem(viewModel.cropBundleList.replacementViewPosition, true)
            viewModel.cropBundleList.setNewRotationDistance(replacementItemPositionPostRemoval)

            // reset page dependent views
            val newPageIndex = viewModel.cropBundleList.newPageIndex()

            textViews.apply {
                setRetentionPercentage(viewModel.cropBundleList.correspondingPosition(viewModel.cropBundleList.replacementViewPosition))
                setPageIndication(newPageIndex, viewModel.cropBundleList.sizePostRemoval)
            }

            seekBar.apply {
                calculateProgressCoefficient(viewModel.cropBundleList.sizePostRemoval)
                indicatePage(newPageIndex)
            }
        }
    }
}