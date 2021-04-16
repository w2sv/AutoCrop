package com.autocrop.activities.examination.imageslider

import android.content.Context
import android.view.MotionEvent
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.FragmentManager
import androidx.viewpager.widget.ViewPager
import com.autocrop.activities.examination.ExaminationActivity
import kotlin.math.abs


private const val CLICK_IDENTIFICATION_THRESHOLD: Int = 100


class CropImageView(
    context: Context,
    private val imageSlider: ViewPager,
    private val position: Int,
    private val imageSliderAdapter: ImageSliderAdapter,
    private val container: ViewGroup,
    private val fragmentManager: FragmentManager
): ImageView(context){

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