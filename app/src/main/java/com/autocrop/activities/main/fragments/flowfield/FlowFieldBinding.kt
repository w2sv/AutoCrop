package com.autocrop.activities.main.fragments.flowfield

import android.graphics.Bitmap
import android.text.SpannableStringBuilder
import android.widget.FrameLayout
import android.widget.ImageButton
import androidx.core.text.color
import androidx.fragment.app.FragmentActivity
import com.autocrop.activities.main.fragments.flowfield.sketch.FlowFieldSketch
import com.autocrop.utils.android.*
import com.autocrop.utils.formattedDateTimeString
import com.w2sv.autocrop.R
import processing.android.PFragment
import processing.core.PGraphics

class FlowFieldBinding(activity: FragmentActivity,
                       canvasContainer: FrameLayout){

    val pFragment: PFragment

    init {
        val (w, h) = screenResolution(activity.windowManager).run {x to y}

        pFragment = PFragment(FlowFieldSketch(w, h))
        pFragment.setView(canvasContainer, activity)
    }

    fun setCaptureButton(flowfieldCaptureButton: ImageButton, permissionsHandler: PermissionsHandler){
        flowfieldCaptureButton.setOnClickListener {
            permissionsHandler.requestPermissionsIfNecessaryAndOrIfAllGrantedRun {
                captureFlowField()
            }
        }
    }

    /**
     * Save current FlowField canvas to "[externalPicturesDir].{FlowField_[formattedDateTimeString]}.jpg",
     * display Snackbar with saving destination
     */
    private fun captureFlowField() =
        with(pFragment.requireActivity()){
            contentResolver.saveBitmap(
                pFragment.sketch.g.bitmap(),
                "FlowField_${formattedDateTimeString()}.jpg"
            )

            displaySnackbar(
                SpannableStringBuilder()
                    .append("Saved FlowField to")
                    .append("\n")
                    .color(getColorInt(NotificationColor.SUCCESS, this)){ append(externalPicturesDir.absolutePath) },
                R.drawable.ic_round_save_24
            )
        }

    private fun PGraphics.bitmap(): Bitmap {
        loadPixels()
        return Bitmap.createBitmap(pixels, width, height, Bitmap.Config.ARGB_8888)
    }
}