package com.autocrop.activities.examination.fragments.examination.viewpager

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Point
import android.os.Handler
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import androidx.viewpager2.widget.ViewPager2.SCROLL_STATE_IDLE
import com.autocrop.CropBundleList
import com.autocrop.activities.examination.fragments.examination.CropActionReactionsPossessor
import com.autocrop.activities.examination.fragments.examination.ExaminationFragment
import com.autocrop.activities.examination.fragments.examination.PageIndicationSeekBar
import com.autocrop.crop
import com.autocrop.cropBundleList
import com.autocrop.utils.*
import com.bunsenbrenner.screenshotboundremoval.R
import com.google.android.material.snackbar.Snackbar
import timber.log.Timber
import java.util.*
import kotlin.math.abs
import kotlin.properties.Delegates


interface ImageActionListener {
    fun onConductedImageAction(cropBundleListPosition: Index, incrementNSavedCrops: Boolean)
}


private typealias Index = Int

private fun Index.rotated(distance: Int, collectionSize: Int): Int =
    plus(distance).run {
        if (smallerThan(0)) {
            (collectionSize - abs(this) % collectionSize) % collectionSize
        } else
            rem(collectionSize)
    }


class ImageSliderAdapter(
    private val textViews: ExaminationFragment.TextViews,
    private val seekBar: PageIndicationSeekBar,
    private val viewPager2: ViewPager2,
    private val context: Context,
    private val fragmentManager: FragmentManager,
    private val cropActionReactionsPossessor: CropActionReactionsPossessor,
    private val displaySnackbar: (String, Int, Int) -> Unit,
    conductAutoScroll: Boolean,
    longAutoScrollDelay: Boolean) : RecyclerView.Adapter<ImageSliderAdapter.ViewHolder>(), ImageActionListener {

    companion object {
        private const val MAX_VIEWS: Int = Int.MAX_VALUE
    }

    private data class ExtendedCropBundleList(
        var tailHash: Int = cropBundleList.last().hashCode(),
        var tailPosition: Index = cropBundleList.lastIndex,
        var headPosition: Index = 0,

        var removePosition: Index? = null) : CropBundleList by cropBundleList {

        fun correspondingPosition(pagerPosition: Int): Int = pagerPosition % size

        val itemToBeRemoved: Boolean
            get() = removePosition != null

        val removingAtTail: Boolean
            get() = removePosition!! == tailPosition

        var rotationDistance by Delegates.notNull<Int>()

        val sizePostRemoval: Int
            get() = lastIndex

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

    private val extendedCropBundleList = ExtendedCropBundleList()

    private var replacementViewPosition by Delegates.notNull<Index>()

    val startPosition: Int = (MAX_VIEWS / 2).run {
        minus(extendedCropBundleList.correspondingPosition(this))
    }

    init {
        seekBar.calculateProgressCoefficient()

        with(viewPager2) {
            registerOnPageChangeCallback(object : OnPageChangeCallback() {

                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)

                    if (!extendedCropBundleList.itemToBeRemoved)
                        with(extendedCropBundleList.correspondingPosition(position)) {
                            textViews.setRetentionPercentage(this)

                            with(extendedCropBundleList.pageIndex(this)) {
                                textViews.setPageIndication(this)
                                seekBar.indicatePage(this)
                            }
                        }
                }

                override fun onPageScrollStateChanged(state: Int) {
                    super.onPageScrollStateChanged(state)

                    if (state == SCROLL_STATE_IDLE && extendedCropBundleList.itemToBeRemoved) {
                        extendedCropBundleList.removeAtRemovePosition()

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

            fun autoScroll() {
                conductingAutoScroll = true
                var conductedScrolls = 0

                val handler = Handler()
                val callback = Runnable {
                    setCurrentItem(currentItem + 1, true)
                    conductedScrolls++
                }

                timer = Timer().apply {
                    schedule(
                        object : TimerTask() {
                            override fun run() {
                                handler.post(callback)

                                if (conductedScrolls == extendedCropBundleList.lastIndex)
                                    cancelScrolling(onScreenTouch = false)
                            }
                        },
                        if (longAutoScrollDelay) 1750L else 1250L,
                        1000
                    )
                }
            }

            if (conductAutoScroll)
                autoScroll()
        }
    }

    lateinit var timer: Timer
    private var conductingAutoScroll: Boolean = false

    private fun cancelScrolling(onScreenTouch: Boolean) {
        timer.cancel()
        conductingAutoScroll = false

        if (onScreenTouch)
            displaySnackbar(
                "Cancelled auto scrolling",
                R.color.light_green,
                Snackbar.LENGTH_SHORT
            )
        else
            displaySnackbar(
                "Scrolled over all crops",
                R.color.magenta,
                Snackbar.LENGTH_SHORT
            )
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
                object : View.OnTouchListener {
                    private var startCoordinates = Point(-1, -1)

                    override fun onTouch(v: View?, event: MotionEvent?): Boolean {

                        // cancel scrolling if currently being conducted
                        if (conductingAutoScroll) {
                            cancelScrolling(onScreenTouch = true)
                            return true
                        }

                        when (event?.action) {
                            MotionEvent.ACTION_DOWN -> startCoordinates = event.coordinates()
                            MotionEvent.ACTION_UP -> if (isClick(event.coordinates()))
                                with(extendedCropBundleList.correspondingPosition(adapterPosition)) {
                                    CropProcedureDialog(
                                        this,
                                        context,
                                        this@ImageSliderAdapter
                                    )
                                        .show(fragmentManager, "Crop procedure dialog")
                                }
                        }
                        return true
                    }

                    private fun MotionEvent.coordinates(): Point = Point(x.toInt(), y.toInt())

                    private fun isClick(endCoordinates: Point): Boolean {
                        val clickIdentificationThreshold = 100

                        return manhattanNorm(
                            startCoordinates,
                            endCoordinates
                        ) < clickIdentificationThreshold
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
        holder.cropImageView.setImageBitmap(
            extendedCropBundleList[extendedCropBundleList.correspondingPosition(
                position
            )].crop
        )
    }

    override fun getItemCount(): Int = if (extendedCropBundleList.size > 1) MAX_VIEWS else 1

    override fun onConductedImageAction(cropBundleListPosition: Index, incrementNSavedCrops: Boolean) {

        // trigger imageActionReactionsPossessor downstream actions
        if (incrementNSavedCrops)
            cropActionReactionsPossessor.incrementNSavedCrops()

        if (extendedCropBundleList.size == 1)
            return cropActionReactionsPossessor.exitActivity()

        extendedCropBundleList.removePosition = cropBundleListPosition

        val replacementCropBundleItemPositionPostRemoval: Index =
            (extendedCropBundleList.removePosition!!).run {
                if (extendedCropBundleList.removingAtTail)
                    Pair(
                        rotated(-1, extendedCropBundleList.sizePostRemoval),
                        viewPager2.currentItem - 1
                    )
                        .also {
                            extendedCropBundleList.tailHash =
                                extendedCropBundleList.at(minus(1)).hashCode()
                            Timber.i("Removing at tail index | Set new dataTailHash")
                        }
                else
                    Pair(
                        if (equals(extendedCropBundleList.lastIndex))
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
        extendedCropBundleList.rotationDistance =
            (replacementViewPosition % extendedCropBundleList.sizePostRemoval) - replacementCropBundleItemPositionPostRemoval

        // reset page dependent views
        val newPageIndex: Int =
            extendedCropBundleList.pageIndex(extendedCropBundleList.removePosition!!).run {
                if (extendedCropBundleList.removingAtTail)
                    minus(1)
                else
                    this
            }

        with(textViews) {
            setRetentionPercentage(
                extendedCropBundleList.correspondingPosition(
                    replacementViewPosition
                )
            )
            setPageIndication(
                newPageIndex,
                extendedCropBundleList.sizePostRemoval
            )
        }

        with(seekBar) {
            calculateProgressCoefficient(extendedCropBundleList.sizePostRemoval)
            indicatePage(newPageIndex)
        }
    }
}