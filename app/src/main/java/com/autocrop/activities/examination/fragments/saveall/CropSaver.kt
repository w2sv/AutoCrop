package com.autocrop.activities.examination.fragments.saveall

import android.content.ContentResolver
import android.os.AsyncTask
import com.autocrop.activities.examination.ExaminationActivityViewModel
import com.autocrop.activities.examination.saveCropAndDeleteScreenshotIfApplicable

class CropSaver(
    private val deleteCorrespondingScreenshots: Boolean,
    private val contentResolver: ContentResolver,
    private val sharedViewModel: ExaminationActivityViewModel,
    private val onTaskFinished: () -> Unit)
        : AsyncTask<Void, Void, Void?>() {

    /**
     * Calls saveCropAndDeleteScreenshotIfApplicable for every crop remaining
     * in cropBundleList
     */
    override fun doInBackground(vararg params: Void): Void? {
        ExaminationActivityViewModel.cropBundles.forEachIndexed { i, bundle ->
            val writeUri = saveCropAndDeleteScreenshotIfApplicable(
                bundle.screenshotUri,
                bundle.crop,
                deleteCorrespondingScreenshots,
                contentResolver
            )
            sharedViewModel.incrementImageFileIOCounters(deleteCorrespondingScreenshots)
            if (i == 0)
                sharedViewModel.setCropWriteDirPathIfApplicable(writeUri)
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