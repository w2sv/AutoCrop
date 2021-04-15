package com.autocrop.activities.examination

import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.net.Uri
import com.autocrop.GlobalParameters
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentManager
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.autocrop.CropWithRetentionPercentage
import kotlin.math.abs

import com.autocrop.activities.main.MainActivity
import com.bunsenbrenner.screenshotboundremoval.R


/**
 *  class holding both cropped images and corresponding uris,
 *  defining sliding/image displaying behavior and inherent side effects such as
 *      the page indication updating
 */
class ImageSliderAdapter(
    private val context: Context,
    private val fm: FragmentManager,
    private val imageSlider: ViewPager,
    val textViews: TextViews): PagerAdapter(){

    val croppedImages: MutableList<CropWithRetentionPercentage> = GlobalParameters.imageCash.values
        .toMutableList()
    val imageUris: MutableList<Uri> = GlobalParameters.imageCash.keys
        .toMutableList().also { GlobalParameters.imageCash.clear() }
    var savedCrops: Int = 0

    init {
        // change page indication text view on slide execution
        class PageChangeListener: ViewPager.SimpleOnPageChangeListener(){
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)

                textViews.setRetentionPercentageText(croppedImages[position].second)
                textViews.setPageIndicationText(position + 1, count)
            }
        }

        imageSlider.addOnPageChangeListener(PageChangeListener())

        textViews.setRetentionPercentageText(croppedImages[0].second)
        textViews.setPageIndicationText(1, count)
    }

    fun returnToMainActivity(){
        ExaminationActivity.toolbarButtonsEnabled = false
        val intent = Intent(
            context,
            MainActivity::class.java
        ).
            apply{this.putExtra(N_SAVED_CROPS, savedCrops)}
        ContextCompat.startActivity(context, intent, null)
    }

    // -----------------
    // OVERRIDES
    // -----------------
    override fun getCount(): Int = croppedImages.size

    override fun getItemPosition(obj: Any): Int = POSITION_NONE

    override fun isViewFromObject(view: View, obj: Any): Boolean = view == obj

    override fun instantiateItem(container: ViewGroup, position: Int): ImageView = ViewPagerImageView(
        context,
        imageSlider,
        position,
        this,
        container,
        fm
    ).apply {
            this.scaleType = ImageView.ScaleType.FIT_CENTER
            this.setImageBitmap(croppedImages[position].first)
            container.addView(this, position)
    }

    override fun destroyItem(container: ViewGroup, position: Int, obj: Any) {}
}


private const val CLICK_IDENTIFICATION_THRESHOLD: Int = 100


private class ViewPagerImageView(
    context: Context,
    private val imageSlider: ViewPager,
    private val position: Int,
    private val imageSliderAdapter: ImageSliderAdapter,
    private val container: ViewGroup,
    private val fragmentManager: FragmentManager): ImageView(context){

    private var startX: Float = 0.toFloat()
    private var startY: Float = 0.toFloat()

    private fun isClick(
        startX: Float,
        startY: Float,
        endX: Float,
        endY: Float
    ): Boolean = (abs(startX - endX) + abs(startY - endY)) < CLICK_IDENTIFICATION_THRESHOLD

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                startX = event.x
                startY = event.y
            }

            MotionEvent.ACTION_UP -> {
                if (isClick(startX, startY, event.x, event.y) && ExaminationActivity.toolbarButtonsEnabled)
                    ProcedureDialog(
                        context,
                        imageSlider,
                        position,
                        imageSliderAdapter,
                        container
                    ).show(fragmentManager, "procedure")
            }
        }
        return true
    }
}