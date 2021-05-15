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


/**
 * Class accounting for procedure dialog message display on screen touch,
 * defining respective procedure effects
 */
class CropProcedureDialog(
    private val cropBundleListPosition: Int,
    private val activityContext: Context,
    private val imageActionListener: ImageActionListener) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        AlertDialog.Builder(activity).run {
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
                    cropBundleListPosition,
                    false
                )
            }
            setPositiveButton(
                "Yes"
            ) { _, _ ->
                saveCropAndDeleteScreenshotIfApplicable(
                    cropBundleList[cropBundleListPosition].crop,
                    cropBundleList[cropBundleListPosition].screenshotUri,
                    activityContext
                )
                imageActionListener.onConductedImageAction(
                    cropBundleListPosition,
                    true
                )
            }
            create()
        }
}