package com.autocrop.activities.examination.viewpager

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Point
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import androidx.viewpager2.widget.ViewPager2.SCROLL_STATE_IDLE
import com.autocrop.activities.examination.ExaminationActivity
import com.autocrop.activities.examination.ImageActionReactionsPossessor
import com.autocrop.crop
import com.autocrop.cropBundleList
import com.autocrop.utils.manhattanNorm
import com.autocrop.utils.toInt
import com.bunsenbrenner.screenshotboundremoval.R
import java.util.*
import kotlin.properties.Delegates


interface ImageActionListener {
    fun onConductedImageAction(position: Int, incrementNSavedCrops: Boolean)
}


class ImageSliderAdapter(
    private val textViews: ExaminationActivity.TextViews,
    private val viewPager2: ViewPager2,
    private val context: Context,
    private val fragmentManager: FragmentManager,
    private val imageActionReactionsPossessor: ImageActionReactionsPossessor,
    private val displayingExitScreen: () -> Boolean
) : RecyclerView.Adapter<ImageSliderAdapter.ViewHolder>(), ImageActionListener {

    private val indices: MutableList<Int> = cropBundleList.indices.toMutableList()

    private var removeDataElementIndex: Int? = null

    private var replacementViewItemIndex by Delegates.notNull<Int>()
    private var dataRotationDistance by Delegates.notNull<Int>()

    val startItemIndex: Int = (Int.MAX_VALUE / 2).run {
        this - dataElementIndex(this)
    }

    init {
        with(viewPager2) {
            registerOnPageChangeCallback(object : OnPageChangeCallback() {

                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)

                    if (removeDataElementIndex == null)
                        with(dataElementIndex(position)){
                            textViews.setRetentionPercentage(this)
                            textViews.setPageIndication(indices[this])
                        }
                }

                override fun onPageScrollStateChanged(state: Int) {
                    super.onPageScrollStateChanged(state)

                    if (state == SCROLL_STATE_IDLE && removeDataElementIndex != null) {
                        val resetMargin = 3

                        cropBundleList.removeAt(removeDataElementIndex!!)
                        Collections.rotate(cropBundleList, dataRotationDistance)

                        with (replacementViewItemIndex){
                            listOf(
                                (this - resetMargin until this),
                                (this + 1..this + resetMargin)
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
                                    .show(fragmentManager, "File io query dialog")
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

    override fun getItemCount(): Int = if (cropBundleList.size > 1) Int.MAX_VALUE else 1

    override fun onConductedImageAction(position: Int, incrementNSavedCrops: Boolean) {
        // trigger imageActionReactionsPossessor downstream actions
        if (incrementNSavedCrops)
            imageActionReactionsPossessor.incrementNSavedCrops()

        if (cropBundleList.size == 1)
            return imageActionReactionsPossessor.exitActivity()

        removeDataElementIndex = dataElementIndex(position)

        val (replacementDataElementIndexPostRemoval: Int, replacementViewItemIndex: Int) = (removeDataElementIndex!!).run {
            if (this == cropBundleList.lastIndex)
                Pair(this - 1, position - 1)
            else
                Pair(this, position + 1)
        }

        viewPager2.setCurrentItem(replacementViewItemIndex, true)
        dataRotationDistance = (replacementViewItemIndex % cropBundleList.lastIndex) - replacementDataElementIndexPostRemoval

        indices.remove(indices.lastIndex)
        Collections.rotate(indices, dataRotationDistance)

        with(textViews) {
            setRetentionPercentage(dataElementIndex(replacementViewItemIndex))
            setPageIndication(indices[replacementDataElementIndexPostRemoval], cropBundleList.lastIndex)
        }

        this.replacementViewItemIndex = replacementViewItemIndex
    }
}