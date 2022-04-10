package com.autocrop.activities.main.fragments.flowfield

import android.text.SpannableStringBuilder
import android.widget.FrameLayout
import android.widget.ImageButton
import androidx.core.text.color
import androidx.fragment.app.FragmentActivity
import com.autocrop.utils.android.*
import com.autocrop.utils.formattedDateTimeString
import processing.android.PFragment

class FlowFieldHandler(private val activity: FragmentActivity,
                       canvasContainer: FrameLayout
){

    val flowfield = FlowFieldPApplet(screenResolution(activity.windowManager))

    init {
        PFragment(flowfield).setView(canvasContainer, activity)
    }

    fun setFlowFieldCaptureButton(flowfieldCaptureButton: ImageButton, permissionsHandler: PermissionsHandler){
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
    private fun captureFlowField(){
        flowfield.bitmap().save(activity.contentResolver,"FlowField_${formattedDateTimeString()}.jpg")

        activity.displaySnackbar(
            SpannableStringBuilder()
                .append("Saved FlowField to")
                .append("\n")
                .color(getColorInt(NotificationColor.SUCCESS, activity)){append(externalPicturesDir.absolutePath)}
        )
    }
}