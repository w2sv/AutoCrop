package com.autocrop.activities.examination.viewpager

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Point
import android.os.Build
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import androidx.viewpager2.widget.ViewPager2.SCROLL_STATE_IDLE
import com.autocrop.activities.examination.ExaminationActivity
import com.autocrop.activities.examination.ImageActionReactionsPossessor
import com.autocrop.crop
import com.autocrop.cropBundleList
import com.autocrop.utils.*
import com.bunsenbrenner.screenshotboundremoval.R
import timber.log.Timber
import java.util.*
import kotlin.math.abs
import kotlin.math.min
import kotlin.properties.Delegates


interface ImageActionListener {
    fun onConductedImageAction(position: Int, incrementNSavedCrops: Boolean)
}


private typealias Index = Int

private fun Index.rotated(distance: Int, collectionSize: Int): Int =
    plus(distance).run{
        if (smallerThan(0)){
            (collectionSize - abs(this) % collectionSize) % collectionSize
        }
        else
            rem(collectionSize)
    }


class ImageSliderAdapter(
    private val textViews: ExaminationActivity.TextViews,
    private val viewPager2: ViewPager2,
    private val context: Context,
    private val fragmentManager: FragmentManager,
    private val imageActionReactionsPossessor: ImageActionReactionsPossessor,
    private val displayingExitScreen: () -> Boolean) : RecyclerView.Adapter<ImageSliderAdapter.ViewHolder>(), ImageActionListener {

    private var dataTailHash: Int = cropBundleList.last().hashCode()
    private var dataTailIndex: Index = cropBundleList.lastIndex

    private var removeDataElementIndex: Index? = null
    private var replacementViewItemIndex by Delegates.notNull<Index>()
    private var dataRotationDistance by Delegates.notNull<Int>()

    val startItemIndex: Int = (N_VIEWS / 2).run {
        minus(dataElementIndex(this))
    }

    companion object{
        private const val VIEW_ITEM_RESET_MARGIN: Int = 3
        private const val N_VIEWS: Int = Int.MAX_VALUE
    }

    init {
        with(viewPager2) {
            registerOnPageChangeCallback(object : OnPageChangeCallback() {

                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)

                    if (removeDataElementIndex == null)
                        with(dataElementIndex(position)){
                            textViews.setRetentionPercentage(this)
                            textViews.setPageIndication(pageIndex(this))
                        }
                }

                override fun onPageScrollStateChanged(state: Int) {
                    super.onPageScrollStateChanged(state)

                    if (state == SCROLL_STATE_IDLE && removeDataElementIndex != null) {
                        cropBundleList.removeAt(removeDataElementIndex!!)

                        Collections.rotate(cropBundleList, dataRotationDistance)
                        dataTailIndex = cropBundleList.indexOfFirst { it.hashCode() == dataTailHash }.also {
                            Timber.i("New data tail index: $it")
                        }

                        with (replacementViewItemIndex){
                            listOf(
                                (minus(VIEW_ITEM_RESET_MARGIN) until this),
                                (plus(1)..plus(VIEW_ITEM_RESET_MARGIN))
                            )
                                .flatten()
                                .forEach { notifyItemChanged(it) }
                        }

                        removeDataElementIndex = null
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
        }
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
                    val clickIdentificationThreshold = 100

                    fun isClick(endCoordinates: Point): Boolean = manhattanNorm(
                        startCoordinates,
                        endCoordinates
                    ) < clickIdentificationThreshold

                    when (event?.action) {
                        MotionEvent.ACTION_DOWN -> startCoordinates = event.coordinates()
                        MotionEvent.ACTION_UP -> {
                            if (isClick(event.coordinates()) && !displayingExitScreen())
                                CropProcedureQueryDialog(
                                    adapterPosition,
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

    private fun dataElementIndex(viewItemIndex: Int): Int = viewItemIndex % cropBundleList.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.slide_item_container_examination_activity, parent, false)
                    as ImageView
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.cropImageView.setImageBitmap(cropBundleList[dataElementIndex(position)].crop)
    }

    override fun getItemCount(): Int = if (cropBundleList.size > 1) N_VIEWS else 1

    private fun pageIndex(dataElementIndex: Index): Index{
        val headIndex: Index = dataTailIndex.rotated(1, cropBundleList.size)

        return if (headIndex <= dataElementIndex)
            dataElementIndex - headIndex
        else
            cropBundleList.lastIndex - headIndex + dataElementIndex + 1
    }

    private fun removingAtDataTail(): Boolean = cropBundleList[removeDataElementIndex!!].hashCode() == dataTailHash

    override fun onConductedImageAction(position: Int, incrementNSavedCrops: Boolean) {

        // trigger imageActionReactionsPossessor downstream actions
        if (incrementNSavedCrops)
            imageActionReactionsPossessor.incrementNSavedCrops()

        if (cropBundleList.size == 1)
            return imageActionReactionsPossessor.exitActivity()

        removeDataElementIndex = dataElementIndex(position)

        val dataSizePostRemoval: Int = cropBundleList.lastIndex
        val lastIndexPostRemoval: Int = cropBundleList.lastIndex - 1

        val (replacementDataElementIndexPostRemoval: Int, replacementViewItemIndex) =
            (removeDataElementIndex!!).run {
                if (removingAtDataTail())
                    Pair(
                        rotated(-1, dataSizePostRemoval),
                        position - 1
                    )
                        .also {
                            Timber.i("Removing at tail index")
                        }
                else
                    Pair(
                        min(this, lastIndexPostRemoval),
                        position + 1
                    )
                        .also {
                            if (it.first == lastIndexPostRemoval)
                                Timber.i("Set to lastIndexPostRemoval")
                        }
            }

        viewPager2.setCurrentItem(replacementViewItemIndex, true)

        dataTailHash = cropBundleList.at(removeDataElementIndex!! - 1).hashCode()
        dataRotationDistance = (replacementViewItemIndex % dataSizePostRemoval) - replacementDataElementIndexPostRemoval

        with(textViews) {
            setRetentionPercentage(dataElementIndex(replacementViewItemIndex))
            setPageIndication(pageIndex(replacementDataElementIndexPostRemoval), dataSizePostRemoval)
        }

        this.replacementViewItemIndex = replacementViewItemIndex
    }
}