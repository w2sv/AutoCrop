package com.autocrop.activities.examination.imageslider

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.autocrop.CropWithRetentionPercentage
import com.autocrop.activities.examination.ExaminationActivity
import com.bunsenbrenner.screenshotboundremoval.R


/**
 *  Class holding both cropped images and corresponding uris,
 *  defining sliding/image displaying behavior and inherent side effects such as
 *      the page indication updating
 */
class ImageSliderAdapter(
    imageSlider: ViewPager2,
    private val croppedImagesWithRetentionPercentages: MutableList<CropWithRetentionPercentage>,
    textViews: ExaminationActivity.ResponsiveTextViews
): RecyclerView.Adapter<ImageSliderAdapter.ViewHolder>() {

    class ViewHolder(view: ImageView) : RecyclerView.ViewHolder(view) {
        val cropImageView: ImageView = view.findViewById(R.id.slide_item_image_view_examination_activity)
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