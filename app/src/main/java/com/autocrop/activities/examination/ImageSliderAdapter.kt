package com.autocrop.activities.examination

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentManager
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import kotlin.math.abs

import com.autocrop.activities.main.MainActivity


/**
 *  class holding both cropped images and corresponding uris,
 *  defining sliding/image displaying behavior and inherent side effects such as
 *      the page indication updating
 */
class ImageSliderAdapter(private val context: Context,
                         private val fm: FragmentManager,
                         private val cr: ContentResolver,
                         private val imageSlider: ViewPager,
                         val pageIndication: TextView,
                         val titleTextView: TextView
): PagerAdapter(){
    val croppedImages: MutableList<Bitmap> = MainActivity.imageCash.values
        .toMutableList()
    val imageUris: MutableList<Uri> = MainActivity.imageCash.keys
        .toMutableList().also { MainActivity.imageCash.clear() }
    var savedCrops: Int = 0

    init {
        // change page indication text view on slide execution
        class PageChangeListener: ViewPager.SimpleOnPageChangeListener(){
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                val displayPosition: Int = position + 1
                pageIndication.setText("$displayPosition/$count  ")
            }
        }
        imageSlider.addOnPageChangeListener(PageChangeListener())
        pageIndication.setText("1/$count  ")
    }

    fun returnToMainActivity(){
        ExaminationActivity.disableSavingButtons = false
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
        cr,
        imageSlider,
        position,
        this,
        container,
        fm).apply {
            this.scaleType = ImageView.ScaleType.FIT_CENTER
            this.setImageBitmap(croppedImages[position])
            container.addView(this, position)
    }

    override fun destroyItem(container: ViewGroup, position: Int, obj: Any) {}
}


private class ViewPagerImageView(context: Context,
                                 private val contentResolver: ContentResolver,
                                 private val imageSlider: ViewPager,
                                 private val position: Int,
                                 private val imageSliderAdapter: ImageSliderAdapter,
                                 private val container: ViewGroup,
                                 private val fragmentManager: FragmentManager): ImageView(context){

    private var startX: Float = 0.toFloat()
    private var startY: Float = 0.toFloat()
    companion object{
        private const val CLICK_MANHATTAN_NORM_THRESHOLD: Int = 100
    }

    private fun isClick(startX: Float,
                        startY: Float,
                        endX: Float,
                        endY: Float): Boolean = (abs(startX - endX) + abs(startY - endY)) < CLICK_MANHATTAN_NORM_THRESHOLD

    override fun onTouchEvent(event: MotionEvent?): Boolean {

        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                startX = event.x
                startY = event.y
            }

            MotionEvent.ACTION_UP -> {

                if (isClick(startX, startY, event.x, event.y) && !ExaminationActivity.disableSavingButtons)
                    ProcedureDialog(
                        context,
                        contentResolver,
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