package com.autocrop.activities.examination

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.viewpager2.widget.ViewPager2
import com.autocrop.activities.SystemUiHidingFragmentActivity
import com.autocrop.activities.cropping.N_DISMISSED_IMAGES_IDENTIFIER
import com.autocrop.activities.examination.imageslider.ImageSliderAdapter
import com.autocrop.activities.examination.imageslider.CubeOutPageTransformer
import com.autocrop.activities.main.MainActivity
import com.autocrop.clearCropBundleList
import com.autocrop.cropBundleList
import com.autocrop.retentionPercentage
import com.autocrop.utils.android.displayToast
import com.autocrop.utils.android.intentExtraIdentifier
import com.bunsenbrenner.screenshotboundremoval.R
import kotlinx.android.synthetic.main.toolbar_examination_activity.*
import timber.log.Timber
import java.lang.ref.WeakReference


val N_SAVED_CROPS: String = intentExtraIdentifier("n_saved_crops")


interface ImageActionReactionsPossessor {
    fun incrementNSavedCrops()
    fun returnToMainActivityOnExhaustedSlider()
}


class ExaminationActivity : SystemUiHidingFragmentActivity(), ImageActionReactionsPossessor {
    private lateinit var imageSlider: ViewPager2
    private lateinit var textViews: TextViews

    private var nSavedCrops: Int = 0
    private var buttonsEnabled: Boolean = true

    inner class TextViews{
        val retentionPercentage: TextView = findViewById(R.id.retention_percentage)
        private val pageIndication: TextView = findViewById(R.id.page_indication)
        private val appTitle: TextView = findViewById(R.id.title_text_view)

        init{
            retentionPercentage.translationX -= (cropBundleList.size.toString().length - 1).let {
                it * 25 + it * 6
            }
            setPageIndicationText(1)
            setRetentionPercentageText(cropBundleList[0].retentionPercentage)
        }

        fun setPageIndicationText(page: Int, nTotalPages: Int = cropBundleList.size) {
            pageIndication.text = getString(R.string.fracture_text, page, nTotalPages)
        }

        fun setRetentionPercentageText(percentage: Int) {
            retentionPercentage.text = getString(R.string.examination_activity_retention_percentage_text, percentage)
        }

        fun renderAppTitleVisible(){
            appTitle.visibility = View.VISIBLE
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /**
         * ViewPager2 Reference: https://medium.com/swlh/android-infinite-auto-image-slider-using-view-pager-2-android-studio-java-a0e450dec071
         */
        fun initializeImageSlider(textViews: TextViews) {
            imageSlider = findViewById<ViewPager2>(R.id.view_pager).apply{
                adapter = ImageSliderAdapter(
                    textViews,
                    this,
                    this@ExaminationActivity,
                    this@ExaminationActivity.supportFragmentManager,
                    this@ExaminationActivity
                ) { buttonsEnabled }
            }
            imageSlider.setPageTransformer(
                CubeOutPageTransformer()
            )
        }

        fun setToolbarButtonOnClickListeners(progressBar: ProgressBar) {

            /**
             * Inherently sets toolbarButtonsEnabled to false if true
             */
            fun toolbarButtonsEnabled(): Boolean {
                return buttonsEnabled.also {
                    if (buttonsEnabled)
                        buttonsEnabled = !buttonsEnabled
                }
            }

            save_all_button.setOnClickListener {
                if (toolbarButtonsEnabled()) {
                    imageSlider.removeAllViews().also {
                        textViews.renderAppTitleVisible()
                    }
                    CropEntiretySaver(
                        WeakReference(progressBar),
                        WeakReference(this),
                        onTaskFinished = this::returnToMainActivity
                    ).execute()

                    nSavedCrops += cropBundleList.size
                }
            }

            dismiss_all_button.setOnClickListener {
                if (toolbarButtonsEnabled()){
                    imageSlider.removeAllViews().also {
                        textViews.renderAppTitleVisible()
                    }
                    returnToMainActivity()
                }
            }
        }

        fun displayDismissedImagesToastIfApplicable(nDismissedImages: Int) {
            when (nDismissedImages) {
                1 -> displayToast("Couldn't find cropping bounds for 1 image")
                in 2..Int.MAX_VALUE -> displayToast("Couldn't find cropping bounds for $nDismissedImages images")
            }
        }

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_NOSENSOR
        setContentView(R.layout.activity_examination)
        textViews = TextViews()

        initializeImageSlider(textViews)
        setToolbarButtonOnClickListeners(progressBar = findViewById(R.id.indeterminateBar))
        displayDismissedImagesToastIfApplicable(
            nDismissedImages = intent.getIntExtra(
                N_DISMISSED_IMAGES_IDENTIFIER,
                0
            )
        )
    }

    // -----------------ImageActionReactionsPossessor overrides-----------------

    override fun incrementNSavedCrops() {
        nSavedCrops += 1
    }

    override fun returnToMainActivityOnExhaustedSlider() {
        with(textViews) {
            renderAppTitleVisible()
            retentionPercentage.visibility = View.INVISIBLE
            setPageIndicationText(69, 420)
        }

        return returnToMainActivity()
    }

    private var backPressedOnce: Boolean = false

    /**
     * Blocked throughout the process of saving all crops,
     * otherwise asks for second one as confirmation;
     *
     * Results in return to main activity
     */
    override fun onBackPressed() {
        val resetDuration: Long = 2500

        // block if saving all / dismissing all
        if (!buttonsEnabled) {
            displayToast("Please wait until crops", "have been saved")
            return
        }

        // return to main activity if already pressed once
        else if (backPressedOnce){
            return returnToMainActivity().also {
                Timber.i("Returning to main activity on second back press")
            }
        }

        // display confirmation prompt toast, set backPressedOnce to true;
        // schedule concurrent reset after reset duration
        backPressedOnce = true
        displayToast("Tap again to return to main screen")

        Handler().postDelayed(
            { backPressedOnce = false },
            resetDuration
        )
    }

    /**
     * Clears remaining cropBundle elements contained within cropBundleList
     */
    private fun returnToMainActivity() {
        buttonsEnabled = false

        return startActivity(
            Intent(
                this,
                MainActivity::class.java
            ).putExtra(N_SAVED_CROPS, nSavedCrops)
        )
    }

    override fun onStop() {
        super.onStop()

        clearCropBundleList()

        finishAndRemoveTask()
        applicationContext.cacheDir.deleteRecursively()
    }
}