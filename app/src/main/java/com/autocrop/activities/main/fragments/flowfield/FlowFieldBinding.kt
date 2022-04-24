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
import timber.log.Timber
import kotlin.math.PI

class FlowFieldBinding(activity: FragmentActivity,
                       canvasContainer: FrameLayout,
                       flowFieldFragmentViewModel: FlowFieldFragmentViewModel){

    private val sketch: FlowFieldSketch
    private val pFragment: PFragment

    init {
        val (w, h) = screenResolution(activity.windowManager).run {x to y}

        sketch = flowFieldFragmentViewModel.flowFieldSketch?.apply {
            setSize(w, h)

//            with(canvas){
//                loadPixels()
//                resize(w, h)
//                updatePixels()
//                Timber.i("screenDims: $w $h | canvasDims: $width $height")
//            }

        } ?: FlowFieldSketch(w, h)

        flowFieldFragmentViewModel.flowFieldSketch = null

        pFragment = PFragment(sketch)
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
            contentResolver.saveBitmap(sketch.canvas.bitmap(), "FlowField_${formattedDateTimeString()}.jpg")

            displaySnackbar(
                SpannableStringBuilder()
                    .append("Saved FlowField to")
                    .append("\n")
                    .color(getColorInt(NotificationColor.SUCCESS, this)){append(externalPicturesDir.absolutePath)},
                R.drawable.ic_round_save_24
            )
        }

    private fun PGraphics.bitmap(): Bitmap {
        loadPixels()
        return Bitmap.createBitmap(pixels, width, height, Bitmap.Config.ARGB_8888)
    }

    fun bindSketchToViewModel(viewModel: FlowFieldFragmentViewModel){
        viewModel.flowFieldSketch = sketch
    }
}