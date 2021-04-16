package com.autocrop.activities.cropping

import android.content.ContentResolver
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import com.autocrop.GlobalParameters
import com.autocrop.PACKAGE_NAME
import com.autocrop.activities.examination.ExaminationActivity
import com.autocrop.activities.main.MainActivity
import com.autocrop.activities.main.croppedImage
import com.bunsenbrenner.screenshotboundremoval.R
import kotlin.math.roundToInt


const val N_DISMISSED_IMAGES_IDENTIFIER: String = "$PACKAGE_NAME.N_DISMISSED_IMAGES"


interface OnTaskCompleted {
    fun onTaskCompleted()
}


class CroppingActivity : AppCompatActivity(), OnTaskCompleted {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_cropping)
        val progressBar: ProgressBar = findViewById(R.id.croppingProgressBar)

        Cropper(
            contentResolver,
            progressBar,
            this
        ).execute()
    }

    override fun onTaskCompleted() {
        Log.i("CroppingActivity", "Async Cropping task finished")

        val nSelectedImages: Int = GlobalParameters.selectedImageUris.size.also {
            GlobalParameters.selectedImageUris.clear()
        }

        // start ExaminationActivity in case of at least 1 successful crop,
        // otherwise return to image selection screen
        if (GlobalParameters.imageCash.isNotEmpty())
            startExaminationActivity(nSelectedImages - GlobalParameters.imageCash.size)
        else
            startMainActivity()
    }

    private fun startExaminationActivity(nDismissedCrops: Int) {
        startActivity(
            Intent(this, ExaminationActivity::class.java).putExtra(
                N_DISMISSED_IMAGES_IDENTIFIER,
                nDismissedCrops
            )
        )
    }

    private fun startMainActivity() {
        startActivity(
            Intent(this, MainActivity::class.java)
        )
    }
}


class Cropper(
    private val contentResolver: ContentResolver,
    private val progressBar: ProgressBar,
    private val listener: OnTaskCompleted
) : AsyncTask<Void, Void, Void?>() {

    override fun doInBackground(vararg params: Void?): Void? {
        val progressBarStep: Int =
            (100.toFloat() / GlobalParameters.selectedImageUris.size.toFloat()).roundToInt()

        GlobalParameters.selectedImageUris.forEach {
            val image: Bitmap? = BitmapFactory.decodeStream(
                contentResolver.openInputStream(it)
            )

            // attempt to crop image, add uri-crop mapping to image cash if successful
            with(croppedImage(image!!)) {
                if (this != null)
                    GlobalParameters.imageCash[it] = this
            }

            // advance progress bar
            progressBar.progress += progressBarStep
        }

        return null
    }

    override fun onPostExecute(result: Void?) {
        super.onPostExecute(result)

        listener.onTaskCompleted()
    }
}