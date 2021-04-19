package com.autocrop.activities.examination

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.os.Handler
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.viewpager.widget.ViewPager
import com.autocrop.*
import com.autocrop.activities.cropping.N_DISMISSED_IMAGES_IDENTIFIER
import com.autocrop.activities.examination.imageslider.ImageSliderAdapter
import com.autocrop.activities.examination.imageslider.ZoomOutPageTransformer
import com.autocrop.activities.hideSystemUI
import com.autocrop.activities.main.MainActivity
import com.autocrop.utils.android.displayToast
import com.autocrop.utils.android.intentExtraIdentifier
import com.bunsenbrenner.screenshotboundremoval.*
import kotlinx.android.synthetic.main.toolbar_examination_activity.*
import java.lang.ref.WeakReference


val N_SAVED_CROPS: String = intentExtraIdentifier("n_saved_crops")


data class ExaminationActivityTextViews(
    val retentionPercentage: TextView,
    val pageIndication: TextView,
    val appTitle: TextView
) {

    fun setPageIndicationText(page: Int, of: Int) {
        pageIndication.text = "$page/$of"
    }

    fun setRetentionPercentageText(percentage: Int) {
        retentionPercentage.text = "$percentage% retained"
    }
}


class ExaminationActivity : FragmentActivity() {
    companion object {
        var toolbarButtonsEnabled: Boolean = true
    }

    private lateinit var imageSlider: ViewPager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_NOSENSOR
        setContentView(R.layout.activity_examination)

        val progressBar: ProgressBar = findViewById(R.id.indeterminateBar)
        val textViews = ExaminationActivityTextViews(
            findViewById(R.id.retention_percentage),
            findViewById(R.id.page_indication),
            findViewById(R.id.title_text_view)
        )

        fun initializeImageSlider() {
            imageSlider = findViewById(R.id.slide)
            imageSlider.setPageTransformer(
                true,
                ZoomOutPageTransformer()
            )
            imageSlider.adapter = ImageSliderAdapter(
                this,
                supportFragmentManager,
                imageSlider,
                textViews
            )
        }

        fun setToolbarButtonOnClickListeners() {

            /**
             * Inherently sets toolbarButtonsEnabled to false if true
             */
            fun toolbarButtonsEnabled(): Boolean {
                if (toolbarButtonsEnabled) {
                    toolbarButtonsEnabled = false
                    return !toolbarButtonsEnabled
                }
                return toolbarButtonsEnabled
            }

            save_all_button.setOnClickListener {
                if (toolbarButtonsEnabled()) {
//                    CropEntiretySaver(
//                        WeakReference(progressBar),
//                        WeakReference(this)
//                    ).execute()
                }
            }

            dismiss_all_button.setOnClickListener {
                if (toolbarButtonsEnabled())
                    returnToMainActivity()
            }
        }

        fun displayDismissedImagesToastIfApplicable(nDismissedImages: Int) {
            when (nDismissedImages) {
                1 -> displayToast("Couldn't find cropping bounds for 1 image")
                in 2..Int.MAX_VALUE -> displayToast("Couldn't find cropping bounds for $nDismissedImages images")
            }
        }

        initializeImageSlider()
        setToolbarButtonOnClickListeners()
        displayDismissedImagesToastIfApplicable(
            nDismissedImages = intent.getIntExtra(N_DISMISSED_IMAGES_IDENTIFIER, 0)
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

    private var backPressedOnce: Boolean = false

    override fun onBackPressed() {
        if (backPressedOnce) {

            returnToMainActivity()
            return
        }

        backPressedOnce = true
        displayToast("Tap again to return to main screen")

        Handler().postDelayed({ backPressedOnce = false }, 2500)
    }

    /**
     * Resets toolbarButtons
     */
    override fun onStop() {
        super.onStop()

        toolbarButtonsEnabled = true
    }

    private fun returnToMainActivity(){
        toolbarButtonsEnabled = false

//        startActivity(
//            Intent(
//            this,
//                MainActivity::class.java
//            ).putExtra(N_SAVED_CROPS, imageSlider.adapter.nSavedCrops)
//        )
    }
}