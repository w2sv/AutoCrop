package com.autocrop.activities.examination.imageslider

import android.content.Context
import android.graphics.Point
import android.net.Uri
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.autocrop.CropWithRetentionPercentage
import com.autocrop.activities.examination.ExaminationActivity
import com.autocrop.activities.examination.ImageActionImpacted
import com.autocrop.utils.toInt
import com.bunsenbrenner.screenshotboundremoval.R
import timber.log.Timber
import kotlin.math.abs


private fun manhattenNorm(a: Point, b: Point): Int = abs(a.x - b.x) + abs(a.y - b.y)


/**
 *  Class holding both cropped images and corresponding uris,
 *  defining sliding/image displaying behavior and inherent side effects such as
 *      the page indication updating
 */
interface ImageActionListener{
    fun onConductedImageAction(sliderPosition: Int, incrementNSavedCrops: Boolean)
}


class ImageSliderAdapter(
    private val imageUris: MutableList<Uri>,
    private val croppedImagesWithRetentionPercentages: MutableList<CropWithRetentionPercentage>,
    private val textViews: ExaminationActivity.TextViews,
    private val imageSlider: ViewPager2,
    private val context: Context,
    private val fragmentManager: FragmentManager,
    private val imageActionImpacted: ImageActionImpacted): RecyclerView.Adapter<ImageSliderAdapter.ViewHolder>(), ImageActionListener {

    override fun onConductedImageAction(sliderPosition: Int, incrementNSavedCrops: Boolean) {
        if (incrementNSavedCrops)
            imageActionImpacted.incrementNSavedCrops()

        if(itemCount == 1) {
            imageSlider.removeViewAt(sliderPosition)
            return imageActionImpacted.returnToMainActivity()
        }

        this.imageUris.removeAt(sliderPosition)
        this.croppedImagesWithRetentionPercentages.removeAt(sliderPosition)

        this.notifyItemRemoved(sliderPosition) // immediately updates itemCount

        val newPosition = listOf(sliderPosition, sliderPosition - 1)[(sliderPosition == itemCount).toInt()]
        textViews.setRetentionPercentageText(croppedImagesWithRetentionPercentages[newPosition].second)
        textViews.setPageIndicationText(newPosition + 1)
    }

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
                            if (isClick(event.coordinates()))
                                ProcedureDialog(
                                    adapterPosition,
                                    imageUris[adapterPosition],
                                    croppedImagesWithRetentionPercentages[adapterPosition].first,
                                    context,
                                    this@ImageSliderAdapter
                                )
                                    .show(fragmentManager, "procedure")
                                    .also{Timber.i("Registered click")}
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

                textViews.setRetentionPercentageText(croppedImagesWithRetentionPercentages[position].second)
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
    override fun getItemCount(): Int = croppedImagesWithRetentionPercentages.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.slide_item_container_examination_activity, parent, false)
                    as ImageView
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.cropImageView.setImageBitmap(croppedImagesWithRetentionPercentages[position].first)
    }
}