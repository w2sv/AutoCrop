package com.autocrop.activities.examination.imageslider

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Point
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
import com.autocrop.activities.examination.ExaminationActivity
import com.autocrop.activities.examination.ImageActionReactionsPossessor
import com.autocrop.crop
import com.autocrop.cropBundleList
import com.autocrop.utils.manhattanNorm
import com.autocrop.utils.toInt
import com.bunsenbrenner.screenshotboundremoval.R
import timber.log.Timber


interface ImageActionListener {
    fun onConductedImageAction(sliderPosition: Int, incrementNSavedCrops: Boolean)
}


class ImageSliderAdapter(
    private val textViews: ExaminationActivity.TextViews,
    private val viewPager2: ViewPager2,
    private val context: Context,
    private val fragmentManager: FragmentManager,
    private val imageActionReactionsPossessor: ImageActionReactionsPossessor,
    private val displayingExitScreen: () -> Boolean
) : RecyclerView.Adapter<ImageSliderAdapter.ViewHolder>(), ImageActionListener {

    private var removeIndex: Int? = null
    private var newlySetIndex: Int = 0

    val startItemIndex: Int = (Int.MAX_VALUE / 2).run {
        this - cropBundleElementIndex(this)
    }

    init {
        with(viewPager2) {
            registerOnPageChangeCallback(object : OnPageChangeCallback() {

                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)

                    if (removeIndex == null)
                        textViews.setPageDependentTexts(cropBundleElementIndex(position))
                }

                override fun onPageScrollStateChanged(state: Int) {
                    super.onPageScrollStateChanged(state)

                    if (state == SCROLL_STATE_IDLE && removeIndex != null) {
                        Timber.i("4REKTUM; removeIndex: $removeIndex; newlySetIndex: $newlySetIndex")

                        with(removeIndex!!) {
                            cropBundleList.removeAt(cropBundleElementIndex(this))

                            (newlySetIndex - 20..newlySetIndex + 20).forEach {
                                if (it != newlySetIndex)
                                    notifyItemChanged(it)
                            }
                        }

                        removeIndex = null
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
                                ImageActionQueryDialog(
                                    adapterPosition,
                                    context,
                                    this@ImageSliderAdapter
                                )
                                    .show(fragmentManager, "procedure")
                        }
                    }
                    return true
                }
            })
        }
    }

    private fun cropBundleElementIndex(viewItemIndex: Int): Int = viewItemIndex % cropBundleList.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.slide_item_container_examination_activity, parent, false)
                    as ImageView
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.cropImageView.setImageBitmap(cropBundleList[cropBundleElementIndex(position)].crop)
    }

    override fun getItemCount(): Int = if (cropBundleList.size > 1) Int.MAX_VALUE else 1

    override fun onConductedImageAction(sliderPosition: Int, incrementNSavedCrops: Boolean) {
        // trigger imageActionReactionsPossessor downstream actions
        if (incrementNSavedCrops)
            imageActionReactionsPossessor.incrementNSavedCrops()

        if (cropBundleList.size == 1)
            return imageActionReactionsPossessor.exitActivity()

        val (subsequentDisplayPageIndex: Int, newPageIndex: Int) = (cropBundleElementIndex(sliderPosition)).run {
            if (this == cropBundleList.lastIndex)
                Pair(sliderPosition - 1, this - 1)
            else
                Pair(sliderPosition + 1, this)
        }

        viewPager2.setCurrentItem(subsequentDisplayPageIndex, true)

        with(textViews) {
            setRetentionPercentage(cropBundleElementIndex(subsequentDisplayPageIndex))
            setPageIndication(newPageIndex, cropBundleList.lastIndex)
        }

        removeIndex = sliderPosition
        newlySetIndex = subsequentDisplayPageIndex
    }
}