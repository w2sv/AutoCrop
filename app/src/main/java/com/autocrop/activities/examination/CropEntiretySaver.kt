package com.autocrop.activities.examination

import android.content.Context
import android.os.AsyncTask
import android.view.View
import android.widget.ProgressBar
import com.autocrop.cropBundleList
import com.autocrop.ops.saveCropAndDeleteScreenshotIfApplicable
import com.autocrop.utils.android.hide
import com.autocrop.utils.android.show
import java.lang.ref.WeakReference


class CropEntiretySaver(
    private val progressBar: WeakReference<ProgressBar>,
    private val textViews: WeakReference<ExaminationActivity.TextViews>,
    private val context: WeakReference<Context>,
    private val onTaskFinished: () -> Unit
) : AsyncTask<Void, Void, Void?>() {

    /**
     * Renders progress bar visible
     */
    override fun onPreExecute() {
        super.onPreExecute()

        textViews.get()!!.saveAll.show()
        progressBar.get()!!.show()
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
                context.get()!!
            )
        }
        return null
    }

    /**
     * Hides progress bar
     */
    override fun onPostExecute(result: Void?) {
        super.onPostExecute(result)

        progressBar.get()!!.hide()

        with(textViews.get()!!) {
            saveAll.hide()
            appTitle.hide()
        }

        onTaskFinished()
    }
}