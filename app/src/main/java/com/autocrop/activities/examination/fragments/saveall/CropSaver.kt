package com.autocrop.activities.examination.fragments.saveall

import android.content.Context
import android.os.AsyncTask
import android.widget.ProgressBar
import com.autocrop.activities.examination.saveCropAndDeleteScreenshotIfApplicable
import com.autocrop.cropBundleList
import com.autocrop.utils.android.hide
import com.autocrop.utils.android.show
import com.autocrop.utils.forceUnwrapped
import java.lang.ref.WeakReference


class CropSaver(
    private val context: WeakReference<Context>,
    private val onTaskFinished: () -> Unit
) : AsyncTask<Void, Void, Void?>() {

    /**
     * Calls saveCropAndDeleteScreenshotIfApplicable for every crop remaining
     * in cropBundleList
     */
    override fun doInBackground(vararg params: Void): Void? {
        for ((uri, bitmap, _) in cropBundleList) {
            saveCropAndDeleteScreenshotIfApplicable(
                uri,
                bitmap,
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