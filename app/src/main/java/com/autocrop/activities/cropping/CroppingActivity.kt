package com.autocrop.activities.cropping

import android.content.ContentResolver
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.autocrop.GlobalParameters
import com.autocrop.activities.examination.ExaminationActivity
import com.autocrop.activities.main.MainActivity
import com.autocrop.activities.main.SELECTED_IMAGE_URI_STRINGS_IDENTIFIER
import com.autocrop.activities.main.croppedImage
import com.autocrop.utils.android.intentExtraIdentifier
import com.autocrop.utils.toInt
import com.autocrop.utils.android.putExtra
import com.bunsenbrenner.screenshotboundremoval.R
import kotlinx.android.synthetic.main.activity_cropping.*
import kotlin.math.roundToInt
import kotlin.properties.Delegates


val N_DISMISSED_IMAGES_IDENTIFIER: String = intentExtraIdentifier("n_dismissed_images")


enum class DismissedImagesQuantity{
    One,
    Multiple;

    companion object {
        fun fromOrdinal(ordinal: Int) = values().first { it.ordinal == ordinal }
    }
}


interface AsyncTaskCompletionListener {
    fun onTaskCompleted()
    fun onTaskCancelled()
}


class CroppingActivity : AppCompatActivity(), AsyncTaskCompletionListener {
    var nSelectedImages by Delegates.notNull<Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // set layout, retrieve views
        setContentView(R.layout.activity_cropping)
        val progressBar: ProgressBar = findViewById(R.id.cropping_progress_bar)
        val currentImageNumberTextView: TextView = findViewById(R.id.cropping_current_image_number_text_view)

        // convert passed image uri strings back to uris, set nSelectedImages
        val imageUris: List<Uri> = intent.getStringArrayExtra(SELECTED_IMAGE_URI_STRINGS_IDENTIFIER)!!.map {
            Uri.parse(it)
        }.also {
            nSelectedImages = it.size
        }

        // execute async cropping task
        Cropper(
            imageUris,
            contentResolver,
            progressBar,
            currentImageNumberTextView,
            this
        ).execute()
    }

    /**
     * Starts either Examination- or MainActivity depending on whether or not any
     * of the selected images has been successfully cropped
     */
    override fun onTaskCompleted() {
        Log.i(this::class.toString(), "Async Cropping task finished")

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
        if (GlobalParameters.imageCash.isNotEmpty())
            startExaminationActivity(nSelectedImages - GlobalParameters.imageCash.size)
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
        )
    }

    override fun onBackPressed() {
        super.onBackPressed()

        GlobalParameters.imageCash.clear()
        println("Called")

        return startMainActivity(putDismissedImagesQuantity = false)
    }

    override fun onTaskCancelled() {
        GlobalParameters.imageCash.clear()
        println("Called")

        return startMainActivity(putDismissedImagesQuantity = false)
    }

    override fun onStop() {
        super.onStop()

        cropping_progress_bar.progress = 0
    }
}


class Cropper(
    private val imageUris: List<Uri>,
    private val contentResolver: ContentResolver,
    private val progressBar: ProgressBar,
    private val currentImageNumberTextView: TextView,
    private val taskCompletionListener: AsyncTaskCompletionListener
    ) : AsyncTask<Void, Void, Void?>() {

    private fun setCurrentImageNumberTextViewText(imageNumber: Int){
        currentImageNumberTextView.text = "$imageNumber/${imageUris.size}"
    }

    override fun onPreExecute() {
        super.onPreExecute()

        setCurrentImageNumberTextViewText(0)
    }

    /**
     * Loads images represented by uris, crops and binds them to imageCash if successful,
     * advances views accordingly
     */
    override fun doInBackground(vararg params: Void?): Void? {
        val (progressBarIntStep: Int, progressBarDecimalStep: Float) = (
                    progressBar.max.toFloat() / imageUris.size.toFloat()
                ).run{ Pair(this.toInt(), this - this.toInt()) }

        var decimalStepSum = 0f

        imageUris.forEachIndexed { index, uri ->
            // load image as bitmap
            val image: Bitmap = BitmapFactory.decodeStream(
                contentResolver.openInputStream(uri)
            )!!

            // attempt to crop image, add uri-crop mapping to image cash if successful
            with(croppedImage(image)) {
                if (this != null)
                    GlobalParameters.imageCash[uri] = this
            }

            setCurrentImageNumberTextViewText(index + 1)

            // advance progress bar
            progressBar.progress += progressBarIntStep

            decimalStepSum += progressBarDecimalStep
            if (decimalStepSum >= 1){
                progressBar.progress += 1
                decimalStepSum -= 1
            }
        }

        return null
    }

    override fun onCancelled() {
        super.onCancelled()

        return taskCompletionListener.onTaskCancelled()
    }

    /**
     * Triggers taskCompletionListener
     */
    override fun onPostExecute(result: Void?) {
        super.onPostExecute(result)

        taskCompletionListener.onTaskCompleted()
    }
}