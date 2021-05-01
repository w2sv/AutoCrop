package com.autocrop.activities.examination.viewpager

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.autocrop.UserPreferences
import com.autocrop.crop
import com.autocrop.cropBundleList
import com.autocrop.ops.saveCropAndDeleteScreenshotIfApplicable
import com.autocrop.screenshotUri
import com.autocrop.utils.android.paddedMessage
import com.autocrop.utils.toInt


/**
 * Class accounting for procedure dialog message display on screen touch,
 * defining respective procedure effects
 */
class CropProcedureQueryDialog(
    private val sliderPositionIndex: Int,
    private val activityContext: Context,
    private val imageActionListener: ImageActionListener) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(this.activity).run {
            this
                .setTitle(
                    paddedMessage(
                        *listOf(
                            listOf("Save crop?"),
                            listOf("Save crop and", "delete screenshot?")
                        )[UserPreferences.deleteInputScreenshots.toInt()].toTypedArray()
                    )
                )
                .setNegativeButton(
                    "No, remove"
                ) { _, _ ->
                    imageActionListener.onConductedImageAction(
                        sliderPositionIndex,
                        false
                    )
                }
                .setPositiveButton(
                    "Yes"
                ) { _, _ ->
                    saveCropAndDeleteScreenshotIfApplicable(
                        cropBundleList[sliderPositionIndex].crop,
                        cropBundleList[sliderPositionIndex].screenshotUri,
                        activityContext
                    )
                    imageActionListener.onConductedImageAction(
                        sliderPositionIndex,
                        true
                    )
                }
                .create()
        }
    }
}