package com.autocrop.activities.examination.fragments.examination.viewpager

import android.annotation.SuppressLint
import android.os.Handler
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import androidx.viewpager2.widget.ViewPager2.SCROLL_STATE_IDLE
import com.autocrop.CropBundleList
import com.autocrop.activities.examination.fragments.examination.ExaminationFragment
import com.autocrop.activities.examination.fragments.examination.PageDismissalImpacted
import com.autocrop.activities.examination.fragments.examination.PageIndicationSeekBar
import com.autocrop.crop
import com.autocrop.cropBundleList
import com.autocrop.utils.*
import com.autocrop.utils.android.TextColors
import com.autocrop.utils.android.displaySnackbar
import com.w2sv.autocrop.R
import com.google.android.material.snackbar.Snackbar
import timber.log.Timber
import java.util.*
import kotlin.properties.Delegates


interface CropActionListener {
    fun onConductedImageAction(cropBundleListPosition: Index, incrementNSavedCrops: Boolean)
}


class ImageSliderAdapter(
    private val textViews: ExaminationFragment.TextViews,
    private val seekBar: PageIndicationSeekBar,
    private val viewPager2: ViewPager2,
    conductAutoScroll: Boolean,
    longAutoScrollDelay: Boolean,
    private val pageDismissalImpacted: PageDismissalImpacted,
    private val activity: FragmentActivity
) : RecyclerView.Adapter<ImageSliderAdapter.ViewHolder>(), CropActionListener {

    companion object {
        private const val MAX_VIEWS: Int = Int.MAX_VALUE
    }

    private data class ExtendedCropBundleList(
        var tailHash: Int = cropBundleList.last().hashCode(),
        var tailPosition: Index = cropBundleList.lastIndex,
        var headPosition: Index = 0,

        var removePosition: Index? = null
    ) : CropBundleList by cropBundleList {

        fun correspondingPosition(pagerPosition: Int): Int = pagerPosition % size

        val itemToBeRemoved: Boolean
            get() = removePosition != null

        val removingAtTail: Boolean
            get() = removePosition!! == tailPosition

        val sizePostRemoval: Int
            get() = lastIndex

        var rotationDistance by Delegates.notNull<Int>()

        fun removeAtRemovePosition() {
            removeAt(removePosition!!)

            Collections.rotate(this, rotationDistance)
            resetPositions()
        }

        private fun resetPositions() {
            with(indexOfFirst { it.hashCode() == tailHash }) {
                tailPosition = this
                headPosition = rotated(1, size)
            }

            removePosition = null
        }

        fun pageIndex(cropBundlePosition: Index): Index =
            headPosition.run {
                if (smallerEquals(cropBundlePosition))
                    cropBundlePosition - this
                else
                    lastIndex - this + cropBundlePosition + 1
            }
    }

    private val data = ExtendedCropBundleList()

    private var replacementViewPosition by Delegates.notNull<Index>()

    val startPosition: Int = (MAX_VIEWS / 2).run {
        minus(data.correspondingPosition(this))
    }

    init {
        seekBar.calculateProgressCoefficient()

        with(viewPager2) {
            registerOnPageChangeCallback(object : OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)

                    if (!data.itemToBeRemoved)
                        with(data.correspondingPosition(position)) {
                            textViews.setRetentionPercentage(this)

                            with(data.pageIndex(this)) {
                                textViews.setPageIndication(this)
                                seekBar.indicatePage(this)
                            }
                        }
                }

                override fun onPageScrollStateChanged(state: Int) {
                    super.onPageScrollStateChanged(state)

                    if (state == SCROLL_STATE_IDLE && data.itemToBeRemoved) {
                        data.removeAtRemovePosition()
                        resetSurroundingPages()
                    }
                }

                private fun resetSurroundingPages() {
                    with(replacementViewPosition) {
                        val resetMargin = 3

                        listOf(
                            (minus(resetMargin) until this),
                            (plus(1)..plus(resetMargin))
                        )
                            .flatten()
                            .forEach { notifyItemChanged(it) }
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

            if (conductAutoScroll)
                scroller = Scroller(
                    viewPager2,
                    longAutoScrollDelay,
                    cropBundleList.lastIndex,
                    activity
                )
        }
    }

    private var scroller: Scroller?
    val scrolling: Boolean
        get() = scroller.run {
            notNull() && this!!.isRunning
        }

    @SuppressLint("ClickableViewAccessibility")
    inner class ViewHolder(view: ImageView) : RecyclerView.ViewHolder(view) {
        val cropImageView: ImageView =
            view.findViewById(R.id.slide_item_image_view_examination_activity)

        /**
         * Sets CropProcedureDialog inflating ImageView onTouchListener
         */
        init {
            cropImageView.setOnTouchListener(
                object : ImmersiveViewOnTouchListener() {
                    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                        Timber.i("Called onTouch")

                        if (scrolling) {
                            scroller!!.cancel(onScreenTouch = true)
                            return true
                        }

                        return super.onTouch(v, event)
                    }

                    override fun onConfirmedTouch() {
                        with(data.correspondingPosition(adapterPosition)) {
                            CropProcedureDialog(
                                this,
                                activity,
                                this@ImageSliderAdapter
                            )
                                .show(activity.supportFragmentManager, "Crop procedure dialog")
                        }
                    }
                }
            )
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.slide_item_container_examination_activity, parent, false)
                    as ImageView
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Index) {
        with(data) {
            holder.cropImageView.setImageBitmap(
                get(correspondingPosition(position))
                    .crop
            )
        }
    }

    override fun getItemCount(): Int = if (data.size > 1) MAX_VIEWS else 1

    override fun onConductedImageAction(
        cropBundleListPosition: Index,
        incrementNSavedCrops: Boolean
    ) {

        // trigger imageActionReactionsPossessor downstream actions
        if (incrementNSavedCrops)
            pageDismissalImpacted.incrementNSavedCrops()

        if (data.size == 1)
            return pageDismissalImpacted.exitActivity()

        data.removePosition = cropBundleListPosition

        val replacementCropBundleItemPositionPostRemoval: Index =
            (data.removePosition!!).run {
                if (data.removingAtTail)
                    Pair(
                        rotated(-1, data.sizePostRemoval),
                        viewPager2.currentItem - 1
                    )
                        .also {
                            data.tailHash =
                                data.at(minus(1)).hashCode()
                            Timber.i("Removing at tail index | Set new dataTailHash")
                        }
                else
                    Pair(
                        if (equals(data.lastIndex))
                            0.also {
                                Timber.i("Set replacementDataElementIndexPostRemoval to lastIndexPostRemoval")
                            }
                        else
                            this,
                        viewPager2.currentItem + 1
                    )
            }
                .run {
                    replacementViewPosition = second
                    first
                }

        // scroll to new view and set cropBundleList rotationDistance
        viewPager2.setCurrentItem(replacementViewPosition, true)
        data.rotationDistance =
            (replacementViewPosition % data.sizePostRemoval) - replacementCropBundleItemPositionPostRemoval

        // reset page dependent views
        val newPageIndex: Int =
            data.pageIndex(data.removePosition!!).run {
                if (data.removingAtTail)
                    minus(1)
                else
                    this
            }

        with(textViews) {
            setRetentionPercentage(
                data.correspondingPosition(
                    replacementViewPosition
                )
            )
            setPageIndication(
                newPageIndex,
                data.sizePostRemoval
            )
        }

        with(seekBar) {
            calculateProgressCoefficient(data.sizePostRemoval)
            indicatePage(newPageIndex)
        }
    }
}


private class Scroller(
    viewPager2: ViewPager2,
    longAutoScrollDelay: Boolean,
    maxScrolls: Int,
    private val activity: FragmentActivity
) {

    private var timer: Timer
    var isRunning: Boolean = true

    init {
        var conductedScrolls = 0

        val handler = Handler()
        val callback = Runnable {
            with(viewPager2) {
                setCurrentItem(currentItem + 1, true)
            }
            conductedScrolls++
        }

        timer = Timer().apply {
            schedule(
                object : TimerTask() {
                    override fun run() {
                        handler.post(callback)

                        if (conductedScrolls >= maxScrolls)
                            cancel(onScreenTouch = false)
                    }
                },
                if (longAutoScrollDelay) 1500L else 1000L,
                1000
            )
        }
    }

    fun cancel(onScreenTouch: Boolean) {
        val textColor: Int = TextColors.successfullyCarriedOut

        timer.cancel()
        isRunning = false

        if (onScreenTouch)
            activity.displaySnackbar(
                "Cancelled auto scrolling",
                textColor,
                Snackbar.LENGTH_SHORT
            )
        else
            activity.displaySnackbar(
                "Traversed all crops",
                textColor,
                Snackbar.LENGTH_SHORT
            )
    }
}