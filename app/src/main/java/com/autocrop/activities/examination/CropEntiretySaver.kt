//package com.autocrop.activities.examination
//
//import android.content.Context
//import android.os.AsyncTask
//import android.view.View
//import android.widget.ProgressBar
//import com.autocrop.activities.examination.imageslider.ImageSliderAdapter
//import com.autocrop.ops.saveCropAndDeleteScreenshotIfApplicable
//import java.lang.ref.WeakReference
//
//
//class CropEntiretySaver(
//    private val progressBar: WeakReference<ProgressBar>,
//    private val context: WeakReference<Context>
//): AsyncTask<Void, Void, Void?>() {
//
//    /**
//     * Renders circular progress bar visible
//     */
//    override fun onPreExecute() {
//        super.onPreExecute()
//
//        progressBar.get()!!.visibility = View.VISIBLE
//    }
//
//    /**
//     * Saves images
//     */
//    override fun doInBackground(vararg params: Void?): Void? {
//        for (i in 0 until sliderAdapter.count){
//            saveCropAndDeleteScreenshotIfApplicable(
//                sliderAdapter.croppedImages[i].first,
//                sliderAdapter.imageUris[i],
//                context.get()!!
//            )
//            sliderAdapter.savedCrops += 1
//        }
//        return null
//    }
//
//    /**
//     * Renders circular progress bar invisible, triggers return
//     * to main activity
//     */
//    override fun onPostExecute(result: Void?) {
//        super.onPostExecute(result)
//
//        progressBar.get()!!.visibility = View.INVISIBLE
//        sliderAdapter.returnToMainActivity()
//    }
//}