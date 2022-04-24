package com.autocrop.activities.main.fragments.flowfield

import android.text.SpannableStringBuilder
import android.widget.FrameLayout
import android.widget.ImageButton
import androidx.core.text.color
import androidx.fragment.app.FragmentActivity
import com.autocrop.utils.android.*
import com.autocrop.utils.formattedDateTimeString
import com.w2sv.autocrop.R
import processing.android.PFragment
import timber.log.Timber

class FlowFieldBinding(activity: FragmentActivity,
                       canvasContainer: FrameLayout,
                       flowFieldFragmentViewModel: FlowFieldFragmentViewModel){

    private val sketch: FlowFieldPApplet
    private val pFragment: PFragment

    init {
        val resolution = screenResolution(activity.windowManager)

        sketch = flowFieldFragmentViewModel.flowFieldSketch?.apply {
            with(canvas){
                loadPixels()

                val w = resolution.x
                val h = resolution.y

                resize(w, h)
//                setSize(w, h)

                updatePixels()

                Timber.i("screenRes: $resolution | canvas dims: $width $height")
            }
        } ?: FlowFieldPApplet(resolution)

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
            contentResolver.saveBitmap(sketch.bitmap(), "FlowField_${formattedDateTimeString()}.jpg")

            displaySnackbar(
                SpannableStringBuilder()
                    .append("Saved FlowField to")
                    .append("\n")
                    .color(getColorInt(NotificationColor.SUCCESS, this)){append(externalPicturesDir.absolutePath)},
                R.drawable.ic_round_save_24
            )
        }

    fun bindSketchToViewModel(viewModel: FlowFieldFragmentViewModel){
        viewModel.flowFieldSketch = sketch
    }
}