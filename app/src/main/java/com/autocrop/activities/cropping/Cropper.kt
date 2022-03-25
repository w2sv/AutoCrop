package com.autocrop.activities.cropping

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.AsyncTask
import android.widget.ProgressBar
import com.autocrop.cropBundleList
import com.autocrop.utils.forceUnwrapped
import com.autocrop.utils.toInt
import java.lang.ref.WeakReference

class Cropper(
    private val viewModel: CroppingActivityViewModel,
    private val progressBar: WeakReference<ProgressBar>,
    private val currentImageNumberTextView: WeakReference<CurrentImageNumberTextView>,
    private val contentResolver: ContentResolver,
    private val onTaskCompleted: () -> Unit)
        : AsyncTask<Uri, Pair<Int, Int>, Void?>() {

    /**
     * Initializes imageOrdinalTextView text
     */
    override fun onPreExecute() {
        super.onPreExecute()

        currentImageNumberTextView.forceUnwrapped().updateText(0)
    }

    /**
     * Loads images represented by uris, crops and binds them to
     * imageBundleList if successful;
     * Publishes incremented progress values to onProgressUpdate
     */
    override fun doInBackground(vararg params: Uri): Void? {
        var decimalStepSum = 0f

        params.forEachIndexed { index, uri ->
            val image: Bitmap = BitmapFactory.decodeStream(contentResolver.openInputStream(uri))!!

            // attempt to crop image, add uri-crop mapping to image cash if successful
            with(croppedImage(image)) {

                // exit loop if task got cancelled
                if (this@Cropper.isCancelled)
                    return null

                this?.let {
                    cropBundleList.add(
                        Triple(uri, first, second)
                    )
                }
            }

            // advance progress bar, screenshot number text view
            decimalStepSum += viewModel.progressBarDecimalStep
            with(Pair(index + 1, viewModel.progressBarIntStep + (decimalStepSum >= 1).toInt())) {
                if (second > viewModel.progressBarIntStep)
                    decimalStepSum -= 1

                publishProgress(this)
            }
        }
        return null
    }

    /**
     * Advances progress bar and imageOrdinalTextView wrt received values
     */
    override fun onProgressUpdate(vararg imageOrdinalWithProgressBarStep: Pair<Int, Int>) {
        with(imageOrdinalWithProgressBarStep[0]) {
            currentImageNumberTextView.forceUnwrapped().updateText(first)
            progressBar.forceUnwrapped().progress += second
        }
    }

    /**
     * Triggers taskCompletionListener
     */
    override fun onPostExecute(result: Void?) {
        super.onPostExecute(result)

        onTaskCompleted()
    }
}