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
import com.autocrop.activities.examination.ExaminationActivity
import com.autocrop.activities.main.N_DISMISSED_IMAGES
import com.autocrop.activities.main.croppedImage
import com.autocrop.utils.android.displayToast
import com.bunsenbrenner.screenshotboundremoval.R
import kotlin.math.roundToInt


class CroppingActivity: AppCompatActivity(){
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_cropping)
        val progressBar: ProgressBar = findViewById(R.id.croppingProgressBar)

        val nSelectedImages: Int = GlobalParameters.selectedImageUris.size

        val task = Cropper(
            contentResolver,
            progressBar
        ).execute()

        if (task.status == AsyncTask.Status.FINISHED){
            Log.i("CroppingActivity", "Received ending of async task")

            // start ExaminationActivity in case of at least 1 successful crop,
            // otherwise return to image selection screen
            if (GlobalParameters.imageCash.isNotEmpty())
                startExaminationActivity(nSelectedImages - GlobalParameters.imageCash.size)
            else
                allImagesDismissedOutput(nSelectedImages > 1)
        }
    }

    private fun startExaminationActivity(nDismissedCrops: Int) {
        startActivity(
            Intent(this, ExaminationActivity::class.java).putExtra(
                N_DISMISSED_IMAGES,
                nDismissedCrops
            )
        )
    }

    private fun allImagesDismissedOutput(attemptedForMultipleImages: Boolean) {
        when (attemptedForMultipleImages) {
            true -> displayToast("Couldn't find cropping bounds for", "any of the selected images")
            false -> displayToast("Couldn't find cropping bounds for selected image")
        }
    }
}


class Cropper(
    private val contentResolver: ContentResolver,
    private val progressBar: ProgressBar): AsyncTask<Void, Void, Void?>() {

    override fun doInBackground(vararg params: Void?): Void? {
        val step: Int = (100.toFloat() / GlobalParameters.selectedImageUris.size.toFloat()).roundToInt()

        GlobalParameters.selectedImageUris.forEach {
            val image: Bitmap? = BitmapFactory.decodeStream(
                contentResolver.openInputStream(it)
            )

            // attempt to crop image, add uri-crop mapping to image cash if successful
            with(croppedImage(image!!)) {
                if (this != null)
                    GlobalParameters.imageCash[it] = this
            }

            progressBar.progress += step
        }

        return null
    }

    override fun onPostExecute(result: Void?) {
        super.onPostExecute(result)

        Log.i("AsyncCropping", "Cropping finished, status=${status}")

        GlobalParameters.selectedImageUris.clear()
    }
}