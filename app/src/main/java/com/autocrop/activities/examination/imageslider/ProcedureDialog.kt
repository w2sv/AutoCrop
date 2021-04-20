package com.autocrop.activities.examination.imageslider

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.viewpager.widget.ViewPager
import com.autocrop.GlobalParameters
import com.autocrop.ops.saveCropAndDeleteScreenshotIfApplicable
import com.autocrop.utils.android.paddedMessage
import com.autocrop.utils.toInt


/**
 * Class accounting for procedure dialog message display on screen touch,
 * defining respective procedure effects
 */
class ProcedureDialog(
    private val position: Int,
    private val activityContext: Context,
    private val imageActionListener: ImageActionListener) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(this.activity).run {
            this
                .setTitle(
                    paddedMessage(
                        *listOf(
                            listOf("Save crop?"),
                            listOf("Save crop and delete", "original screenshot?")
                        )[GlobalParameters.deleteInputScreenshots.toInt()].toTypedArray()
                    )
                )
                .setNegativeButton(
                    "No",
                    object: DialogInterface.OnClickListener{
                        override fun onClick(dialog: DialogInterface, which: Int){
                            imageActionListener.onConductedImageAction(
                                position,
                                false
                            )
                        }
                    }
                )
                .setPositiveButton(
                    "Yes",
                    object: DialogInterface.OnClickListener{
                        override fun onClick(dialog: DialogInterface, which: Int){
                            saveCropAndDeleteScreenshotIfApplicable(
                                GlobalParameters.cropBundleList[position].second,
                                GlobalParameters.cropBundleList[position].first,
                                activityContext
                            )
                            imageActionListener.onConductedImageAction(
                                position,
                                true
                            )
                        }
                    }
                )

            this.create()
        }
    }
}