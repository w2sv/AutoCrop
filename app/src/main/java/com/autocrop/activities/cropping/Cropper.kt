package com.autocrop.activities.cropping

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
    private val nSelectedImages: Int,
    private val context: WeakReference<Context>,
    private val progressBar: WeakReference<ProgressBar>,
    private val setCurrentImageViewText: (Int) -> Unit,
    private val taskCompletionListener: CroppingCompletionListener
) : AsyncTask<Uri, Pair<Int, Int>, Void?>() {

    /**
     * Initializes imageOrdinalTextView text
     */
    override fun onPreExecute() {
        super.onPreExecute()

        setCurrentImageViewText(0)
    }

    /**
     * Loads images represented by uris, crops and binds them to
     * imageBundleList if successful;
     * Publishes incremented progress values to onProgressUpdate
     */
    override fun doInBackground(vararg params: Uri): Void? {
        var decimalStepSum = 0f

        val (progressBarIntStep: Int, progressBarDecimalStep: Float) = (
                progressBar.forceUnwrapped().max.toFloat() / nSelectedImages.toFloat()
                )
            .run {
                toInt()
                    .let {
                        Pair(it, this - it)
                    }
            }

        params.forEachIndexed { index, uri ->
            val image: Bitmap = BitmapFactory.decodeStream(
                context.forceUnwrapped().contentResolver.openInputStream(uri)
            )!!

            // attempt to crop image, add uri-crop mapping to image cash if successful
            with(croppedImage(image)) {

                // exit loop if task got cancelled
                if (this@Cropper.isCancelled)
                    return null
                else if (this != null)
                    cropBundleList.add(
                        Triple(uri, first, second)
                    )
            }

            // advance progress bar, image ordinal text view
            decimalStepSum += progressBarDecimalStep
            with(Pair(index + 1, progressBarIntStep + (decimalStepSum >= 1).toInt())) {
                if (this.second > progressBarIntStep)
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
            setCurrentImageViewText(first)

            // advance progress bar
            progressBar.forceUnwrapped().progress += second
        }
    }

    /**
     * Triggers taskCompletionListener
     */
    override fun onPostExecute(result: Void?) {
        super.onPostExecute(result)

        taskCompletionListener.onTaskCompleted()
    }
}