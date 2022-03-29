package com.autocrop.activities.examination.fragments.viewpager

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.autocrop.UserPreferences
import com.autocrop.activities.examination.saveCropAndDeleteScreenshotIfApplicable


/**
 * Class accounting for procedure dialog display upon screen click,
 * defining respective procedure effects
 */
class CropProcedureDialog(
    private val cropWithUri: Pair<Uri, Bitmap>,
    private val imageFileWritingContext: Context,
    private val onCropAction: (incrementNSavedCrops: Boolean) -> Unit)
        : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        AlertDialog.Builder(activity).run {
            setTitle("Save crop?")

            setMultiChoiceItems(arrayOf("Delete corresponding screenshot"), booleanArrayOf(UserPreferences.deleteIndividualScreenshot)){_, _, _ -> UserPreferences.toggle(UserPreferences.Keys.deleteIndividualScreenshot) }
            setNegativeButton("No, discard") { _, _ -> onCropAction(false)}
            setPositiveButton("Yes") { _, _ ->
                CropProcessor().execute(IOParameters(cropWithUri.first, cropWithUri.second, UserPreferences.deleteIndividualScreenshot, imageFileWritingContext))
                onCropAction(true)
            }

            create()
        }

    private data class IOParameters(val uri: Uri, val crop: Bitmap, val deleteScreenshot: Boolean, val context: Context)

    private class CropProcessor: AsyncTask<IOParameters, Void, Void?>() {
        override fun doInBackground(vararg params: IOParameters): Void? {
            with(params.first()) {
                saveCropAndDeleteScreenshotIfApplicable(
                    uri, crop, deleteScreenshot, context
                )
            }
            return null
        }
    }
}