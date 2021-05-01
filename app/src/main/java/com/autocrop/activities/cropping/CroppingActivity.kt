package com.autocrop.activities.cropping

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.autocrop.activities.BackPressHandler
import com.autocrop.activities.examination.ExaminationActivity
import com.autocrop.activities.hideSystemUI
import com.autocrop.activities.main.MainActivity
import com.autocrop.activities.main.SELECTED_IMAGE_URI_STRINGS_IDENTIFIER
import com.autocrop.clearCropBundleList
import com.autocrop.cropBundleList
import com.autocrop.utils.android.*
import com.autocrop.utils.toInt
import com.bunsenbrenner.screenshotboundremoval.R
import kotlinx.android.synthetic.main.activity_cropping.*
import timber.log.Timber
import java.lang.ref.WeakReference
import kotlin.properties.Delegates


val N_DISMISSED_IMAGES_IDENTIFIER: String = intentExtraIdentifier("n_dismissed_images")


interface CroppingCompletionListener {
    fun onTaskCompleted()
}


class CroppingActivity : AppCompatActivity(), CroppingCompletionListener {
    private var nSelectedImages by Delegates.notNull<Int>()
    private lateinit var croppingTask: Cropper

    inner class Views{
        val progressBar: ProgressBar = findViewById(R.id.cropping_progress_bar)
        val currentImageNumberText: TextView = findViewById(R.id.cropping_current_image_number_text_view)
        val croppingText: TextView = findViewById(R.id.cropping_text_view)

        val couldntFindCroppingBoundsText: TextView = findViewById(R.id.couldnt_find_cropping_bounds_text_view)
        val couldntFindCroppingBoundsIcon: TextView = findViewById(R.id.couldnt_find_cropping_bounds_icon)

        val croppingViews: List<View>
            get() = listOf(progressBar, currentImageNumberText, croppingText)

        val couldntFindCroppingBoundsViews: List<View>
            get() = listOf(couldntFindCroppingBoundsText, couldntFindCroppingBoundsIcon)
    }

    lateinit var views: Views

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // set layout, retrieve views
        setContentView(R.layout.activity_cropping)
        views = Views()

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
            WeakReference(views.progressBar),
            WeakReference(views.currentImageNumberText),
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

        // start ExaminationActivity in case of at least 1 successful crop,
        // otherwise return to MainActivity
        if (cropBundleList.isNotEmpty())
            startExaminationActivity(nSelectedImages - cropBundleList.size)
        else{
            Handler().postDelayed(
                {
                    hideSystemUI(window)

                    with(views) {
                        croppingViews.forEach {
                            it.hide()
                        }

                        couldntFindCroppingBoundsText.text =
                            R.string.couldnt_find_any_cropping_bounds.run {
                                if (nSelectedImages > 1)
                                    getString(this, " any of", "s")
                                else
                                    getString(this, "", "")
                            }

                        couldntFindCroppingBoundsViews.forEach {
                            it.show()
                        }

                        Handler().postDelayed(
                            { startMainActivity() },
                            3000
                        )
                    }
                },
                300
            )
        }
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

    private val backPressHandler = BackPressHandler()

    override fun onBackPressed() {
        if (croppingTask.status == AsyncTask.Status.FINISHED)
            return

        if (backPressHandler.pressedOnce){
            croppingTask.cancel(false)
            clearCropBundleList()

            return startMainActivity()
        }

        backPressHandler.onPress()
        displayToast("Tap again to cancel")
    }

    override fun onStop() {
        super.onStop()

        cropping_progress_bar.progress = 0
        finishAndRemoveTask()
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

        setImageOrdinalText(0)
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
            setImageOrdinalText(first)

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

    private fun setImageOrdinalText(imageOrdinal: Int) {
        imageOrdinalTextView.get()!!.text = context.get()!!.getString(R.string.fracture_text, imageOrdinal, nSelectedImages)
    }
}