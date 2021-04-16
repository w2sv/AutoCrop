package com.autocrop.activities.examination

import android.content.Context
import android.os.AsyncTask
import android.view.View
import android.widget.ProgressBar
import com.autocrop.activities.examination.imageslider.ImageSliderAdapter


// TODO: deal with context leaking fields

class CropEntiretySaver(
    val progressBar: ProgressBar,
    val sliderAdapter: ImageSliderAdapter,
    val context: Context
): AsyncTask<Void, Void, Void?>() {

    /**
     * Renders circular progress bar visible
     */
    override fun onPreExecute() {
        super.onPreExecute()

        progressBar.visibility = View.VISIBLE
    }

    /**
     * Saves images
     */
    override fun doInBackground(vararg params: Void?): Void? {
        for (i in 0 until sliderAdapter.count){
            saveImageAndDeleteScreenshotIfApplicable(
                sliderAdapter.imageUris[i],
                sliderAdapter.croppedImages[i].first,
                context
            )
            sliderAdapter.savedCrops += 1
        }
        return null
    }

    /**
     * Renders circular progress bar invisible, triggers return
     * to main activity
     */
    override fun onPostExecute(result: Void?) {
        super.onPostExecute(result)

        progressBar.visibility = View.INVISIBLE
        sliderAdapter.returnToMainActivity()
    }
}