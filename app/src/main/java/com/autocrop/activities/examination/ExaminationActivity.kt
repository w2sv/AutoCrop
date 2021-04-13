package com.autocrop.activities.examination

import android.content.ContentResolver
import android.content.Context
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.viewpager.widget.ViewPager
import kotlinx.android.synthetic.main.toolbar.*

import com.autocrop.*
import com.autocrop.activities.main.N_DISMISSED_IMAGES
import com.bunsenbrenner.screenshotboundremoval.*


const val N_SAVED_CROPS: String = "$PACKAGE_NAME.N_SAVED_CROPS"


fun saveCroppedAndDeleteOriginal(
    imageUri: Uri,
    croppedImage: Bitmap,
    context: Context,
    cr: ContentResolver) {

    if (ExaminationActivity.deleteInputScreenshots)
        imageUri.deleteUnderlyingResource(context)

    saveImage(
        cr,
        croppedImage,
        imageUri.getRealPath(context)
    )
}


class ExaminationActivity : FragmentActivity() {
    private lateinit var imageSlider: ViewPager
    private lateinit var sliderAdapter: ImageSliderAdapter

    companion object{
        var deleteInputScreenshots: Boolean = true

        fun toggleDeleteInputScreenshots(){
            deleteInputScreenshots = !deleteInputScreenshots
        }
        var disableSavingButtons: Boolean = true
    }

    override fun onStart() {
        super.onStart()
        hideSystemUI(window)
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        hideSystemUI(window)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_NOSENSOR

        // set layout, retrieve layout elements
        setContentView(R.layout.activity_examination)
        val progressBar: ProgressBar = findViewById(R.id.indeterminateBar)
        val pageIndication: TextView = findViewById(R.id.page_indication)
        val titleTextView: TextView = findViewById(R.id.title_text_view)

        // if applicable display message informing about images which couldn't be cropped
        intent.getIntExtra(N_DISMISSED_IMAGES, 0).run{
            when (this){
                1 -> displayToast("Couldn't find cropping bounds for 1 image")
                in 2..Int.MAX_VALUE -> displayToast("Couldn't find cropping bounds for $this images")
            }
        }

        // initialize image slider
        imageSlider = findViewById(R.id.slide)
        imageSlider.apply{
            this.setPageTransformer(true,
                ZoomOutPageTransformer()
            )
            sliderAdapter =
                ImageSliderAdapter(
                    this@ExaminationActivity,
                    supportFragmentManager,
                    contentResolver,
                    imageSlider,
                    pageIndication,
                    titleTextView
                )
            this.adapter = sliderAdapter
        }

        // set toolbar button onClickListeners
        save_all_button.setOnClickListener{
            if (!disableSavingButtons){
                disableSavingButtons = true
                AsyncSaveAllOnClickExecutor(
                    progressBar,
                    sliderAdapter,
                    this,
                    contentResolver
                ).execute()
            }
        }

        dismiss_all_button.setOnClickListener{
            if (!disableSavingButtons){
                disableSavingButtons = true
                sliderAdapter.returnToMainActivity()
            }
        }
    }

    /**
     * display saving result message on back button press
     */
    override fun onBackPressed() {
        sliderAdapter.returnToMainActivity()
    }
}


private class AsyncSaveAllOnClickExecutor(val progressBar: ProgressBar,
                                          val sliderAdapter: ImageSliderAdapter,
                                          val context: Context,
                                          val contentResolver: ContentResolver): AsyncTask<Void, Void, Void?>() {
    override fun onPreExecute() {
        super.onPreExecute()
        progressBar.visibility = View.VISIBLE
    }

    override fun doInBackground(vararg params: Void?): Void? {
        for (i in 0 until sliderAdapter.count){
            saveCroppedAndDeleteOriginal(
                sliderAdapter.imageUris[i],
                sliderAdapter.croppedImages[i],
                context,
                contentResolver
            )
            sliderAdapter.savedCrops += 1
        }
        return null
    }

    override fun onPostExecute(result: Void?) {
        super.onPostExecute(result)
        progressBar.visibility = View.INVISIBLE
        sliderAdapter.returnToMainActivity()
    }
}