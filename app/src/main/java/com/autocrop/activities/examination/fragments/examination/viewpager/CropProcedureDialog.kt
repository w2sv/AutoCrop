package com.autocrop.activities.examination.fragments.examination.viewpager

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import com.autocrop.UserPreferences
import com.autocrop.activities.examination.saveCropAndDeleteScreenshotIfApplicable
import com.autocrop.utils.android.TaggedDialogFragment
import com.autocrop.utils.get


/**
 * Class accounting for procedure dialog display upon screen click,
 * defining respective procedure effects
 */
class CropProcedureDialog(
    private val cropWithUri: Pair<Uri, Bitmap>,
    private val imageFileWritingContext: Context,
    private val onCropAction: (incrementNSavedCrops: Boolean) -> Unit)
        : TaggedDialogFragment() {

    private val title: String = listOf(
        "Save crop?",
        "Save crop and\ndelete screenshot?"
    )[UserPreferences.deleteInputScreenshots]

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        AlertDialog.Builder(activity).run {
            setTitle(title)

            setNegativeButton("No, discard") { _, _ -> onCropAction(false)}
            setPositiveButton("Yes") { _, _ ->
                CropProcessor().execute(Triple(cropWithUri.first, cropWithUri.second, imageFileWritingContext))
                onCropAction(true)
            }

            create()
        }

    private class CropProcessor: AsyncTask<Triple<Uri, Bitmap, Context>, Void, Void?>() {
        override fun doInBackground(vararg params: Triple<Uri, Bitmap, Context>): Void? {
            with(params.first()) {
                saveCropAndDeleteScreenshotIfApplicable(
                    first,
                    second,
                    third
                )
            }
            return null
        }
    }
}