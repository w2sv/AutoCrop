package com.autocrop.activities.examination.cardfragments.back

import android.content.Context
import android.os.AsyncTask
import android.widget.ProgressBar
import com.autocrop.activities.examination.ExaminationActivity
import com.autocrop.activities.examination.saveCropAndDeleteScreenshotIfApplicable
import com.autocrop.cropBundleList
import com.autocrop.utils.android.hide
import com.autocrop.utils.android.show
import com.autocrop.utils.forceUnwrapped
import java.lang.ref.WeakReference


class CropSaver(
    private val progressBar: WeakReference<ProgressBar>,
    private val textViews: WeakReference<CardBackFragment.TextViews>,
    private val context: WeakReference<Context>,
    private val onTaskFinished: () -> Unit
) : AsyncTask<Void, Void, Void?>() {

    /**
     * Renders progress bar visible
     */
    override fun onPreExecute() {
        super.onPreExecute()

        textViews.forceUnwrapped().saveAll.show()
        progressBar.forceUnwrapped().show()
    }

    /**
     * Calls saveCropAndDeleteScreenshotIfApplicable for every crop remaining
     * in cropBundleList
     */
    override fun doInBackground(vararg params: Void): Void? {
        for ((uri, bitmap, _) in cropBundleList) {
            saveCropAndDeleteScreenshotIfApplicable(
                bitmap,
                uri,
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

        progressBar.forceUnwrapped().hide()

        with(textViews.forceUnwrapped()) {
            saveAll.hide()
            appTitle.show()
        }

        onTaskFinished()
    }
}