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
import com.autocrop.GlobalParameters
import com.autocrop.activities.examination.ExaminationActivity
import com.autocrop.activities.examination.ImageActionImpacted
import com.autocrop.crop
import com.autocrop.retentionPercentage
import com.autocrop.utils.toInt
import com.bunsenbrenner.screenshotboundremoval.R
import timber.log.Timber
import kotlin.math.abs


private fun manhattenNorm(a: Point, b: Point): Int = abs(a.x - b.x) + abs(a.y - b.y)


interface ImageActionListener{
    fun onConductedImageAction(sliderPosition: Int, incrementNSavedCrops: Boolean)
}


class ImageSliderAdapter(
    private val textViews: ExaminationActivity.TextViews,
    private val imageSlider: ViewPager2,
    private val context: Context,
    private val fragmentManager: FragmentManager,
    private val imageActionImpacted: ImageActionImpacted,
    private val buttonsEnabled: () -> Boolean): RecyclerView.Adapter<ImageSliderAdapter.ViewHolder>(), ImageActionListener {

    override fun onConductedImageAction(sliderPosition: Int, incrementNSavedCrops: Boolean) {
        if (incrementNSavedCrops)
            imageActionImpacted.incrementNSavedCrops()

        if(itemCount == 1) {
            imageSlider.removeViewAt(sliderPosition)
            return imageActionImpacted.returnToMainActivityOnExhaustedSlider()
        }

        GlobalParameters.cropBundleList.removeAt(sliderPosition)

        this.notifyItemRemoved(sliderPosition) // immediately updates itemCount

        val newPosition = listOf(sliderPosition, sliderPosition - 1)[(sliderPosition == itemCount).toInt()]
        textViews.setRetentionPercentageText(GlobalParameters.cropBundleList[newPosition].retentionPercentage())
        textViews.setPageIndicationText(newPosition + 1)
    }

    @SuppressLint("ClickableViewAccessibility")
    inner class ViewHolder(view: ImageView) : RecyclerView.ViewHolder(view) {
        val cropImageView: ImageView = view.findViewById(R.id.slide_item_image_view_examination_activity)

        init {
            cropImageView.setOnTouchListener( object: View.OnTouchListener{
                private var startCoordinates = Point(-1, -1)
                private fun MotionEvent.coordinates(): Point = Point(x.toInt(), y.toInt())

                override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                    val CLICK_IDENTIFICATION_THRESHOLD = 100

                    fun isClick(endCoordinates: Point): Boolean = manhattenNorm(startCoordinates, endCoordinates) < CLICK_IDENTIFICATION_THRESHOLD

                    when (event?.action) {
                        MotionEvent.ACTION_DOWN -> startCoordinates = event.coordinates()
                        MotionEvent.ACTION_UP -> {
                            if (isClick(event.coordinates()) && buttonsEnabled())
                                ProcedureDialog(
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

    init {
        imageSlider.registerOnPageChangeCallback(object : OnPageChangeCallback() {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels)
            }

            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)

                textViews.setRetentionPercentageText(GlobalParameters.cropBundleList[position].retentionPercentage())
                textViews.setPageIndicationText(position + 1)
            }

            override fun onPageScrollStateChanged(state: Int) {
                super.onPageScrollStateChanged(state)
            }
        })
    }

    // -----------------
    // OVERRIDES
    // -----------------
    override fun getItemCount(): Int = GlobalParameters.cropBundleList.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.slide_item_container_examination_activity, parent, false)
                    as ImageView
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.cropImageView.setImageBitmap(GlobalParameters.cropBundleList[position].crop())
    }
}