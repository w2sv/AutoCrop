package com.autocrop.activities.examination

import android.content.Intent
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.widget.ViewPager2
import com.autocrop.*
import com.autocrop.activities.cropping.N_DISMISSED_IMAGES_IDENTIFIER
import com.autocrop.activities.examination.imageslider.ImageSliderAdapter
import com.autocrop.activities.hideSystemUI
import com.autocrop.activities.main.MainActivity
import com.autocrop.utils.android.displayToast
import com.autocrop.utils.android.intentExtraIdentifier
import com.bunsenbrenner.screenshotboundremoval.*
import kotlinx.android.synthetic.main.toolbar_examination_activity.*
import java.lang.ref.WeakReference


val N_SAVED_CROPS: String = intentExtraIdentifier("n_saved_crops")


interface ImageActionImpacted{
    fun incrementNSavedCrops()
    fun returnToMainActivity()
}


class ExaminationActivity : FragmentActivity(), ImageActionImpacted {
    override fun incrementNSavedCrops(){
        nSavedCrops += 1
    }

    override fun returnToMainActivity(){
        textViews.retentionPercentage.visibility = View.INVISIBLE
        textViews.setPageIndicationText(69)
        textViews.appTitle.visibility = View.VISIBLE

        toMainActivity()
    }

    private lateinit var imageSlider: ViewPager2

    // TODO
    val croppedImagesWithRetentionPercentages: MutableList<CropWithRetentionPercentage> = GlobalParameters.imageCash.values
        .toMutableList()
    val imageUris: MutableList<Uri> = GlobalParameters.imageCash.keys
        .toMutableList().also {
            GlobalParameters.clearImageCash()
        }
    var nSavedCrops: Int = 0

    var toolbarButtonsEnabled: Boolean = true

    inner class TextViews(
        val retentionPercentage: TextView,
        private val pageIndication: TextView,
        val appTitle: TextView) {

        fun setPageIndicationText(page: Int) {
            pageIndication.text = "$page/${croppedImagesWithRetentionPercentages.size}"
        }

        fun setRetentionPercentageText(percentage: Int) {
            retentionPercentage.text = "$percentage% retained"
        }
    }

    lateinit var textViews: TextViews

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_NOSENSOR
        setContentView(R.layout.activity_examination)

        val progressBar: ProgressBar = findViewById(R.id.indeterminateBar)

        textViews = TextViews(
            findViewById(R.id.retention_percentage),
            findViewById(R.id.page_indication),
            findViewById(R.id.title_text_view)
        ).apply {
            this.setRetentionPercentageText(croppedImagesWithRetentionPercentages[0].second)
            this.setPageIndicationText(1)
        }

        fun initializeImageSlider() {
            imageSlider = findViewById(R.id.view_pager)
//            imageSlider.setPageTransformer(
//                ZoomOutPageTransformer()
//            )
            imageSlider.adapter = ImageSliderAdapter(
                imageUris,
                croppedImagesWithRetentionPercentages,
                textViews,
                imageSlider,
                this,
                this.supportFragmentManager,
                this
            )
        }

        fun setToolbarButtonOnClickListeners() {

            /**
             * Inherently sets toolbarButtonsEnabled to false if true
             */
            fun toolbarButtonsEnabled(): Boolean {
                return toolbarButtonsEnabled.also {
                    if (toolbarButtonsEnabled)
                        toolbarButtonsEnabled = !toolbarButtonsEnabled
                }
            }

            save_all_button.setOnClickListener {
                if (toolbarButtonsEnabled()) {
                    CropEntiretySaver(
                        WeakReference(progressBar),
                        WeakReference(this),
                        onTaskFinished = this::toMainActivity
                    ).execute(*(imageUris zip croppedImagesWithRetentionPercentages.map { it.first }).toTypedArray())

                    nSavedCrops += croppedImagesWithRetentionPercentages.size
                }
            }

            dismiss_all_button.setOnClickListener {
                if (toolbarButtonsEnabled())
                    toMainActivity()
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
        if (!toolbarButtonsEnabled){
            displayToast("Please wait until crops", "have been saved")
            return
        }

        else if (backPressedOnce) {
            return toMainActivity()
        }

        backPressedOnce = true
        displayToast("Tap again to return to main screen")

        Handler().postDelayed({ backPressedOnce = false }, 2500)
    }

    private fun toMainActivity(){
        toolbarButtonsEnabled = false

        startActivity(
            Intent(
            this,
                MainActivity::class.java
            ).putExtra(N_SAVED_CROPS, nSavedCrops)
        )
    }
}