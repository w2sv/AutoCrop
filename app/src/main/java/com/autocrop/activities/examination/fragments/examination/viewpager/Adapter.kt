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
    fun onConductedImageAction(position: Int, incrementNSavedCrops: Boolean)
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
    longAutoScrollDelay: Boolean
) : RecyclerView.Adapter<ImageSliderAdapter.ViewHolder>(), ImageActionListener {

    private var dataTailHash: Int = cropBundleList.last().hashCode()
    private var dataTailIndex: Index = cropBundleList.lastIndex

    private var removeDataIndex: Index? = null
    private var replacementViewItemIndex by Delegates.notNull<Index>()
    private var dataRotationDistance by Delegates.notNull<Int>()

    companion object {
        private const val N_VIEWS: Int = Int.MAX_VALUE

        private fun dataIndex(pagerPosition: Int): Int = pagerPosition % cropBundleList.size
    }

    val startItemIndex: Int = (N_VIEWS / 2).run {
        minus(dataIndex(this))
    }

    init {
        seekBar.calculateProgressCoefficient()

        with(viewPager2) {
            registerOnPageChangeCallback(object : OnPageChangeCallback() {

                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)

                    if (removeDataIndex == null)
                        with(dataIndex(position)) {
                            textViews.setRetentionPercentage(this)

                            with(pageIndex(this)) {
                                textViews.setPageIndication(this)
                                seekBar.indicatePage(this)
                            }
                        }
                }

                override fun onPageScrollStateChanged(state: Int) {
                    super.onPageScrollStateChanged(state)

                    if (state == SCROLL_STATE_IDLE && removeDataIndex != null) {
                        cropBundleList.removeAt(removeDataIndex!!)

                        Collections.rotate(cropBundleList, dataRotationDistance)
                        dataTailIndex =
                            cropBundleList.indexOfFirst { it.hashCode() == dataTailHash }

                        with(replacementViewItemIndex) {
                            val resetMargin = 3

                            listOf(
                                (minus(resetMargin) until this),
                                (plus(1)..plus(resetMargin))
                            )
                                .flatten()
                                .forEach { notifyItemChanged(it) }
                        }

                        removeDataIndex = null
                    }
                }
            })

            /**
             * Reference: https://www.loginworks.com/blogs/how-to-make-awesome-transition-effects-using-pagetransformer-in-android/
             */
            setPageTransformer { view: View, position: Float ->
                with(view) {
                    pivotX = listOf(0f, width.toFloat())[(position < 0f).toInt()]
                    pivotY = height * 0.5f
                    rotationY = 90f * position
                }
            }

            fun autoScroll(){
                conductingAutoScroll = true
                var conductedScrolls = 0

                val handler = Handler()
                val callback = Runnable{
                    setCurrentItem(currentItem + 1, true)
                    conductedScrolls++
                }

                timer = Timer().apply {
                    schedule(
                        object : TimerTask() {
                            override fun run() {
                                handler.post(callback)

                                if (conductedScrolls == cropBundleList.lastIndex)
                                    cancelScrolling(onScreenTouch = false)
                            }
                        },
                        if (longAutoScrollDelay) 1500L else 1250L,
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

    private fun cancelScrolling(onScreenTouch: Boolean){
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

        init {
            cropImageView.setOnTouchListener(object : View.OnTouchListener {
                private var startCoordinates = Point(-1, -1)
                private fun MotionEvent.coordinates(): Point = Point(x.toInt(), y.toInt())

                override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                    if (conductingAutoScroll){
                        cancelScrolling(onScreenTouch = true)
                        return true
                    }

                    val clickIdentificationThreshold = 100

                    fun isClick(endCoordinates: Point): Boolean = manhattanNorm(
                        startCoordinates,
                        endCoordinates
                    ) < clickIdentificationThreshold

                    when (event?.action) {
                        MotionEvent.ACTION_DOWN -> startCoordinates = event.coordinates()
                        MotionEvent.ACTION_UP -> if (isClick(event.coordinates()))
                            with(dataIndex(adapterPosition)){
                                removeDataIndex = this

                                CropProcedureQueryDialog(
                                    adapterPosition,
                                    this,
                                    context,
                                    this@ImageSliderAdapter
                                )
                                    .show(fragmentManager, "File procedure query dialog")
                            }
                    }
                    return true
                }
            })
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.slide_item_container_examination_activity, parent, false)
                    as ImageView
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.cropImageView.setImageBitmap(cropBundleList[dataIndex(position)].crop)
    }

    override fun getItemCount(): Int = if (cropBundleList.size > 1) N_VIEWS else 1

    private fun pageIndex(dataElementIndex: Index): Index {
        val headIndex: Index = dataTailIndex.rotated(1, cropBundleList.size)

        return if (headIndex <= dataElementIndex)
            dataElementIndex - headIndex
        else
            cropBundleList.lastIndex - headIndex + dataElementIndex + 1
    }

    override fun onConductedImageAction(position: Int, incrementNSavedCrops: Boolean) {

        // trigger imageActionReactionsPossessor downstream actions
        if (incrementNSavedCrops)
            cropActionReactionsPossessor.incrementNSavedCrops()

        if (cropBundleList.size == 1)
            return cropActionReactionsPossessor.exitActivity()

        val removingAtDataTail: Boolean = cropBundleList[removeDataIndex!!].hashCode() == dataTailHash
        val dataSizePostRemoval: Int = cropBundleList.size - 1

        val (replacementDataElementIndexPostRemoval: Int, replacementViewItemIndex) =
            (removeDataIndex!!).run {
                if (removingAtDataTail)
                    Pair(
                        rotated(-1, dataSizePostRemoval),
                        position - 1
                    )
                        .also {
                            dataTailHash = cropBundleList.at(minus(1)).hashCode()
                            Timber.i("Removing at tail index | Set new dataTailHash")
                        }
                else
                    Pair(
                        if (equals(cropBundleList.lastIndex))
                            0.also {
                                Timber.i("Set replacementDataElementIndexPostRemoval to lastIndexPostRemoval")
                            }
                        else
                            this,
                        position + 1
                    )
            }

        viewPager2.setCurrentItem(replacementViewItemIndex, true)
        dataRotationDistance =
            (replacementViewItemIndex % dataSizePostRemoval) - replacementDataElementIndexPostRemoval

        val newPageIndex: Int = pageIndex(removeDataIndex!!).run {
            if (removingAtDataTail)
                minus(1)
            else
                this
        }

        with(textViews) {
            setRetentionPercentage(dataIndex(replacementViewItemIndex))
            setPageIndication(
                newPageIndex,
                dataSizePostRemoval
            )
        }

        with(seekBar) {
            calculateProgressCoefficient(dataSizePostRemoval)
            indicatePage(newPageIndex)
        }

        this.replacementViewItemIndex = replacementViewItemIndex
    }
}