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

    init {
        viewPager2.registerOnPageChangeCallback(object : OnPageChangeCallback() {

            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)

                textViews.setPageDependentTexts(position)
            }
        })

        viewPager2.setPageTransformer(
            CubeOutPageTransformer()
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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.slide_item_container_examination_activity, parent, false)
                    as ImageView
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.cropImageView.setImageBitmap(cropBundleList[position].crop)
    }

    override fun getItemCount(): Int = cropBundleList.size

    override fun onConductedImageAction(sliderPosition: Int, incrementNSavedCrops: Boolean) {
        // trigger imageActionReactionsPossessor downstream actions
        if (incrementNSavedCrops)
            imageActionReactionsPossessor.incrementNSavedCrops()

        if (itemCount == 1)
            return imageActionReactionsPossessor.exitActivity()

        cropBundleList.removeAt(sliderPosition).also {
            notifyItemRemoved(sliderPosition) // updates itemCount
            notifyDataSetChanged()
        }

        viewPager2.setCurrentItem(viewPager2.currentItem, true)
        textViews.setPageDependentTexts(pageIndex = viewPager2.currentItem)

        Timber.i("!Slider Position: $sliderPosition; Current item: ${viewPager2.currentItem}")
    }
}