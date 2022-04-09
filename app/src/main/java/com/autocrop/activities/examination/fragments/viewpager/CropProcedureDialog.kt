package com.autocrop.activities.examination.fragments.viewpager

import android.app.AlertDialog
import android.app.Dialog
import android.content.ContentResolver
import android.graphics.Bitmap
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.autocrop.activities.examination.ExaminationActivityViewModel
import com.autocrop.activities.examination.saveCropAndDeleteScreenshotIfApplicable
import com.autocrop.global.BooleanUserPreferences

/**
 * Class accounting for procedure dialog display upon screen click,
 * defining respective procedure effects
 */
class CropProcedureDialog(
    private val cropWithUri: Pair<Uri, Bitmap>,
    private val contentResolver: ContentResolver,
    private val sharedViewModel: ExaminationActivityViewModel,
    private val onCropAction: () -> Unit)
        : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        AlertDialog.Builder(activity).run {
            setTitle("Save crop?")

            setMultiChoiceItems(arrayOf("Delete corresponding screenshot"), booleanArrayOf(
                BooleanUserPreferences.deleteIndividualScreenshot)){ _, _, _ -> BooleanUserPreferences.toggle(
                BooleanUserPreferences.Keys.deleteIndividualScreenshot) }
            setNegativeButton("No, discard") { _, _ -> onCropAction()}
            setPositiveButton("Yes") { _, _ ->
                CropProcessor(sharedViewModel).execute(IOParameters(cropWithUri.first, cropWithUri.second, BooleanUserPreferences.deleteIndividualScreenshot, contentResolver))
                onCropAction()
            }

            create()
        }

    private data class IOParameters(val uri: Uri, val crop: Bitmap, val deleteScreenshot: Boolean, val contentResolver: ContentResolver)

    private class CropProcessor(private val sharedViewModel: ExaminationActivityViewModel): AsyncTask<IOParameters, Void, Void?>() {
        override fun doInBackground(vararg params: IOParameters): Void? =
            params.first().run {
                val writeUri = saveCropAndDeleteScreenshotIfApplicable(uri, crop, deleteScreenshot, contentResolver)

                sharedViewModel.setCropWriteDirPathIfApplicable(writeUri)
                sharedViewModel.incrementImageFileIOCounters(deleteScreenshot)
                null
            }
    }
}