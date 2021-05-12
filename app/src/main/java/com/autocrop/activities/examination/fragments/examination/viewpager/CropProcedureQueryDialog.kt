package com.autocrop.activities.examination.fragments.examination.viewpager

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.autocrop.UserPreferences
import com.autocrop.activities.examination.saveCropAndDeleteScreenshotIfApplicable
import com.autocrop.crop
import com.autocrop.cropBundleList
import com.autocrop.screenshotUri
import com.autocrop.utils.getByBoolean
import com.autocrop.utils.toInt


/**
 * Class accounting for procedure dialog message display on screen touch,
 * defining respective procedure effects
 */
class CropProcedureQueryDialog(
    private val sliderPositionIndex: Int,
    private val activityContext: Context,
    private val imageActionListener: ImageActionListener
) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        AlertDialog.Builder(this.activity).run {
            setTitle(
                listOf(
                    "Save crop?",
                    "Save crop and\ndelete screenshot?"
                ).getByBoolean(UserPreferences.deleteInputScreenshots)
            )

            setNegativeButton(
                "No, remove"
            ) { _, _ ->
                imageActionListener.onConductedImageAction(
                    sliderPositionIndex,
                    false
                )
            }
            setPositiveButton(
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
            create()
        }
}