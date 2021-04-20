package com.autocrop.activities.examination

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.AsyncTask
import android.view.View
import android.widget.ProgressBar
import com.autocrop.ops.saveCropAndDeleteScreenshotIfApplicable
import java.lang.ref.WeakReference


class CropEntiretySaver(
    private val progressBar: WeakReference<ProgressBar>,
    private val context: WeakReference<Context>,
    private val onTaskFinished: () -> Unit
): AsyncTask<Pair<Uri, Bitmap>, Void, Void?>() {

    /**
     * Renders circular progress bar visible
     */
    override fun onPreExecute() {
        super.onPreExecute()

        progressBar.get()!!.visibility = View.VISIBLE
    }

    /**
     * Saves images
     */
    override fun doInBackground(vararg params: Pair<Uri, Bitmap>): Void? {
        for ((uri, bitmap) in params){
            saveCropAndDeleteScreenshotIfApplicable(
                bitmap,
                uri,
                context.get()!!
            )
        }
        return null
    }

    /**
     * Renders circular progress bar invisible
     */
    override fun onPostExecute(result: Void?) {
        super.onPostExecute(result)

        progressBar.get()!!.visibility = View.INVISIBLE
        onTaskFinished()
    }
}