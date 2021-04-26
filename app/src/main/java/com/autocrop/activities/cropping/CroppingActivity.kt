package com.autocrop.activities.cropping

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.autocrop.activities.examination.ExaminationActivity
import com.autocrop.activities.main.MainActivity
import com.autocrop.activities.main.SELECTED_IMAGE_URI_STRINGS_IDENTIFIER
import com.autocrop.clearCropBundleList
import com.autocrop.cropBundleList
import com.autocrop.utils.android.intentExtraIdentifier
import com.autocrop.utils.android.putExtra
import com.autocrop.utils.toInt
import com.bunsenbrenner.screenshotboundremoval.R
import kotlinx.android.synthetic.main.activity_cropping.*
import timber.log.Timber
import java.lang.ref.WeakReference
import kotlin.properties.Delegates


val N_DISMISSED_IMAGES_IDENTIFIER: String = intentExtraIdentifier("n_dismissed_images")


enum class DismissedImagesQuantity {
    One,
    Multiple;

    companion object {
        fun fromOrdinal(ordinal: Int): DismissedImagesQuantity =
            values().first { it.ordinal == ordinal }
    }
}


interface CroppingCompletionListener {
    fun onTaskCompleted()
}


class CroppingActivity : AppCompatActivity(), CroppingCompletionListener {
    private var nSelectedImages by Delegates.notNull<Int>()
    private lateinit var croppingTask: Cropper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // set layout, retrieve views
        setContentView(R.layout.activity_cropping)
        val progressBar: ProgressBar = findViewById(R.id.cropping_progress_bar)
        val currentImageNumberTextView: TextView =
            findViewById(R.id.cropping_current_image_number_text_view)

        // convert passed image uri strings back to uris, set nSelectedImages
        val imageUris: Array<Uri> =
            intent.getStringArrayExtra(SELECTED_IMAGE_URI_STRINGS_IDENTIFIER)!!.map {
                Uri.parse(it)
            }
                .toTypedArray()
                .also {
                    nSelectedImages = it.size
                }

        // execute async cropping task
        croppingTask = Cropper(
            nSelectedImages,
            WeakReference(this),
            WeakReference(progressBar),
            WeakReference(currentImageNumberTextView),
            this
        ).also {
            it.execute(*imageUris)
        }
    }

    /**
     * Starts either Examination- or MainActivity depending on whether or not any
     * of the selected images has been successfully cropped
     */
    override fun onTaskCompleted() {
        Timber.i("Async Cropping task finished")

        fun startExaminationActivity(nDismissedCrops: Int) {
            startActivity(
                Intent(this, ExaminationActivity::class.java).putExtra(
                    N_DISMISSED_IMAGES_IDENTIFIER,
                    nDismissedCrops
                )
            )
        }

        // start ExaminationActivity in case of at least 1 successful crop,
        // otherwise return to MainActivity
        if (cropBundleList.isNotEmpty())
            startExaminationActivity(nSelectedImages - cropBundleList.size)
        else
            startMainActivity(putDismissedImagesQuantity = true)
    }

    private fun startMainActivity(putDismissedImagesQuantity: Boolean) {
        startActivity(
            Intent(this, MainActivity::class.java).apply {
                if (putDismissedImagesQuantity)
                    this.putExtra(
                        DismissedImagesQuantity.fromOrdinal(
                            (nSelectedImages > 1).toInt()
                        )
                    )
            }
        ).also {
            finishAndRemoveTask()
        }
    }

    override fun onBackPressed() {
        croppingTask.cancel(false).also {
            Timber.i(
                listOf(
                    "Couldn't cancel cropping task",
                    "Cropping task successfully cancelled"
                )[it.toInt()]
            )
        }
        clearCropBundleList()

        startMainActivity(putDismissedImagesQuantity = false)
    }

    /**
     * Resets progress bar progress
     */
    override fun onStop() {
        super.onStop()

        cropping_progress_bar.progress = 0
    }
}


class Cropper(
    private val nSelectedImages: Int,
    private val context: WeakReference<Context>,
    private val progressBar: WeakReference<ProgressBar>,
    private val imageOrdinalTextView: WeakReference<TextView>,
    private val taskCompletionListener: CroppingCompletionListener
) : AsyncTask<Uri, Pair<Int, Int>, Void?>() {

    /**
     * Initializes imageOrdinalTextView text
     */
    override fun onPreExecute() {
        super.onPreExecute()

        imageOrdinalTextView.get()!!.setImageOrdinalText(0)
    }

    /**
     * Loads images represented by uris, crops and binds them to
     * imageBundleList if successful;
     * Publishes incremented progress values to onProgressUpdate
     */
    override fun doInBackground(vararg params: Uri): Void? {
        var decimalStepSum = 0f

        val (progressBarIntStep: Int, progressBarDecimalStep: Float) = (
                progressBar.get()!!.max.toFloat() / nSelectedImages.toFloat()
                ).run { Pair(this.toInt(), this - this.toInt()) }

        params.forEachIndexed { index, uri ->
            val image: Bitmap = BitmapFactory.decodeStream(
                context.get()!!.contentResolver.openInputStream(uri)
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
            imageOrdinalTextView.get()?.setImageOrdinalText(first)

            // advance progress bar
            progressBar.get()?.let {
                it.progress += second
            }
        }
    }

    /**
     * Triggers taskCompletionListener
     */
    override fun onPostExecute(result: Void?) {
        super.onPostExecute(result)

        taskCompletionListener.onTaskCompleted()
    }

    private fun TextView.setImageOrdinalText(imageOrdinal: Int) {
        text = context.getString(R.string.fracture_text, imageOrdinal, nSelectedImages)
    }
}