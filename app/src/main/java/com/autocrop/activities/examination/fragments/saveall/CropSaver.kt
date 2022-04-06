package com.autocrop.activities.examination.fragments.saveall

import android.content.ContentResolver
import android.os.AsyncTask
import com.autocrop.activities.examination.ExaminationActivityViewModel
import com.autocrop.activities.examination.saveCropAndDeleteScreenshotIfApplicable


class CropSaver(
    private val deleteCorrespondingScreenshots: Boolean,
    private val contentResolver: ContentResolver,
    private val incrementImageFileIOCounters: (Boolean) -> Unit,
    private val onTaskFinished: () -> Unit)
        : AsyncTask<Void, Void, Void?>() {

    /**
     * Calls saveCropAndDeleteScreenshotIfApplicable for every crop remaining
     * in cropBundleList
     */
    override fun doInBackground(vararg params: Void): Void? {
        for ((uri, bitmap, _) in ExaminationActivityViewModel.cropBundles) {
            saveCropAndDeleteScreenshotIfApplicable(
                uri,
                bitmap,
                deleteCorrespondingScreenshots,
                contentResolver
            )
            incrementImageFileIOCounters(deleteCorrespondingScreenshots)
        }
        return null
    }

    /**
     * Hides progress bar
     */
    override fun onPostExecute(result: Void?) {
        super.onPostExecute(result)

        onTaskFinished()
    }
}