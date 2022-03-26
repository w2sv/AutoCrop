package com.autocrop.activities.examination.fragments.singleaction.saveall

import android.content.Context
import android.os.AsyncTask
import com.autocrop.activities.examination.ExaminationActivity
import com.autocrop.activities.examination.saveCropAndDeleteScreenshotIfApplicable
import com.autocrop.utils.forceUnwrapped
import java.lang.ref.WeakReference


class CropSaver(
    private val deleteCorrespondingScreenshots: Boolean,
    private val context: WeakReference<Context>,
    private val onTaskFinished: () -> Unit)
        : AsyncTask<Void, Void, Void?>() {

    /**
     * Calls saveCropAndDeleteScreenshotIfApplicable for every crop remaining
     * in cropBundleList
     */
    override fun doInBackground(vararg params: Void): Void? {
        for ((uri, bitmap, _) in ExaminationActivity.cropBundles) {
            saveCropAndDeleteScreenshotIfApplicable(
                uri,
                bitmap,
                deleteCorrespondingScreenshots,
                context.forceUnwrapped()
            )
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