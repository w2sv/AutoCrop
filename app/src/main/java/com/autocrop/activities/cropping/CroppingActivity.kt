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
}


class CroppingActivity : AppCompatActivity(), AsyncTaskCompletionListener {
    var nSelectedImages by Delegates.notNull<Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // set layout, retrieve views
        setContentView(R.layout.activity_cropping)
        val progressBar: ProgressBar = findViewById(R.id.croppingProgressBar)

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

        fun startMainActivity() {
            startActivity(
                Intent(this, MainActivity::class.java).putExtra(
                    DismissedImagesQuantity.fromOrdinal(
                        (nSelectedImages > 1).toInt()
                    )
                )
            )
        }

        // start ExaminationActivity in case of at least 1 successful crop,
        // otherwise return to MainActivity
        if (GlobalParameters.imageCash.isNotEmpty())
            startExaminationActivity(nSelectedImages - GlobalParameters.imageCash.size)
        else
            startMainActivity()
    }
}


class Cropper(
    private val imageUris: List<Uri>,
    private val contentResolver: ContentResolver,
    private val progressBar: ProgressBar,
    private val taskCompletionListener: AsyncTaskCompletionListener
    ) : AsyncTask<Void, Void, Void?>() {

    /**
     * Loads images represented by uris, crops and binds them to imageCash if successful,
     * advances progress bar accordingly
     */
    override fun doInBackground(vararg params: Void?): Void? {
        val progressBarStep: Int =
            (100.toFloat() / imageUris.size.toFloat()).roundToInt()

        imageUris.forEach {

            // load image as bitmap
            val image: Bitmap = BitmapFactory.decodeStream(
                contentResolver.openInputStream(it)
            )!!

            // attempt to crop image, add uri-crop mapping to image cash if successful
            with(croppedImage(image)) {
                if (this != null)
                    GlobalParameters.imageCash[it] = this
            }

            // advance progress bar
            progressBar.progress += progressBarStep
        }

        return null
    }

    /**
     * Triggers taskCompletionListener
     */
    override fun onPostExecute(result: Void?) {
        super.onPostExecute(result)

        taskCompletionListener.onTaskCompleted()
    }
}