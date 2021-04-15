package com.autocrop.activities.examination

import android.content.Context
import android.content.pm.ActivityInfo
import android.os.AsyncTask
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.viewpager.widget.ViewPager
import com.autocrop.*
import com.autocrop.activities.hideSystemUI
import com.autocrop.activities.main.N_DISMISSED_IMAGES
import com.autocrop.utils.displayToast
import com.bunsenbrenner.screenshotboundremoval.*
import kotlinx.android.synthetic.main.toolbar_examination_activity.*


const val N_SAVED_CROPS: String = "$PACKAGE_NAME.N_SAVED_CROPS"


data class TextViews(
    val retentionPercentage: TextView,
    val pageIndication: TextView,
    val appTitle: TextView
)


class ExaminationActivity : FragmentActivity() {
    companion object{
        var toolbarButtonsEnabled: Boolean = true
    }

    private lateinit var imageSlider: ViewPager
    private lateinit var sliderAdapter: ImageSliderAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_NOSENSOR
        setContentView(R.layout.activity_examination)

        val progressBar: ProgressBar = findViewById(R.id.indeterminateBar)
        val textViews = TextViews(
            findViewById(R.id.retention_percentage),
            findViewById(R.id.page_indication),
            findViewById(R.id.title_text_view)
        )

        fun initializeImageSlider(){
            imageSlider = findViewById(R.id.slide)
            imageSlider.apply{
                this.setPageTransformer(
                    true,
                    ZoomOutPageTransformer()
                )
                sliderAdapter =
                    ImageSliderAdapter(
                        this@ExaminationActivity,
                        supportFragmentManager,
                        imageSlider,
                        textViews
                    )
                this.adapter = sliderAdapter
            }
        }

        fun setToolbarButtonOnClickListeners(){

            /**
             * Inherently sets toolbarButtonsEnabled to false if true
             */
            fun toolbarButtonsEnabled(): Boolean{
                if (toolbarButtonsEnabled){
                    toolbarButtonsEnabled = false
                    return !toolbarButtonsEnabled
                }
                return toolbarButtonsEnabled
            }

            save_all_button.setOnClickListener{
                if (toolbarButtonsEnabled()){
                    AsyncSaveAllOnClickExecutor(
                        progressBar,
                        sliderAdapter,
                        this
                    ).execute()
                }
            }

            dismiss_all_button.setOnClickListener{
                if (toolbarButtonsEnabled())
                    sliderAdapter.returnToMainActivity()
            }
        }

        fun displayDismissedImagesToastIfApplicable(nDismissedImages: Int){
            when (nDismissedImages){
                1 -> displayToast("Couldn't find cropping bounds for 1 image")
                in 2..Int.MAX_VALUE -> displayToast("Couldn't find cropping bounds for $this images")
            }
        }

        initializeImageSlider()
        setToolbarButtonOnClickListeners()
        displayDismissedImagesToastIfApplicable(
            intent.getIntExtra(N_DISMISSED_IMAGES, 0)
        )
    }

    override fun onStart() {
        super.onStart()
        hideSystemUI(window)
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        hideSystemUI(window)
    }

    override fun onBackPressed() {
        sliderAdapter.returnToMainActivity()
    }

    /**
     * Resets toolbarButtons
     */
    override fun onStop() {
        super.onStop()

        toolbarButtonsEnabled = true
    }
}


private class AsyncSaveAllOnClickExecutor(
    val progressBar: ProgressBar,
    val sliderAdapter: ImageSliderAdapter,
    val context: Context
): AsyncTask<Void, Void, Void?>() {

    override fun onPreExecute() {
        super.onPreExecute()

        progressBar.visibility = View.VISIBLE
    }

    override fun doInBackground(vararg params: Void?): Void? {
        for (i in 0 until sliderAdapter.count){
            saveImageAndDeleteScreenshotIfApplicable(
                sliderAdapter.imageUris[i],
                sliderAdapter.croppedImages[i].first,
                context
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