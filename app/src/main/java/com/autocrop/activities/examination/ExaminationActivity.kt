/**
 * ViewPager2 Reference: https://medium.com/swlh/android-infinite-auto-image-slider-using-view-pager-2-android-studio-java-a0e450dec071
 */

package com.autocrop.activities.examination

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.viewpager2.widget.ViewPager2
import com.autocrop.activities.BackPressHandler
import com.autocrop.activities.SystemUiHidingFragmentActivity
import com.autocrop.activities.cropping.N_DISMISSED_IMAGES_IDENTIFIER
import com.autocrop.activities.examination.imageslider.ImageSliderAdapter
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
    fun exitActivity()
}


class ExaminationActivity : SystemUiHidingFragmentActivity(), ImageActionReactionsPossessor {
    private lateinit var viewPager2: ViewPager2
    private lateinit var textViews: TextViews
    private lateinit var toolbar: Toolbar

    private var nSavedCrops: Int = 0
    private var displayingExitScreen: Boolean = false

    inner class TextViews {
        private val retentionPercentage: TextView = findViewById(R.id.retention_percentage)
        private val pageIndication: TextView = findViewById(R.id.page_indication)
        private val appTitle: TextView = findViewById(R.id.title_text_view)

        init {
            retentionPercentage.translationX -= (cropBundleList.size.toString().length - 1).let {
                it * 25 + it * 6
            }
            setPageDependentTexts(0)
        }

        fun setPageDependentTexts(pageIndex: Int = viewPager2.currentItem) {
            with (pageIndex){
                setPageIndication(this)
                setRetentionPercentage(this)
            }
        }

        fun setPageIndication(pageIndex: Int = viewPager2.currentItem, itemCount: Int = cropBundleList.size){
            pageIndication.text = getString(
                R.string.fracture_text,
                pageIndex + 1,
                itemCount
            )
        }

        fun setRetentionPercentage(pageIndex: Int){
            retentionPercentage.text = getString(
                R.string.examination_activity_retention_percentage_text,
                cropBundleList[pageIndex].retentionPercentage
            )
        }

        fun renderAppTitleVisible() {
            appTitle.visibility = View.VISIBLE
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        fun initializeImageSlider(textViews: TextViews) {
            viewPager2 = findViewById<ViewPager2>(R.id.view_pager).apply {
                adapter = ImageSliderAdapter(
                    textViews,
                    this,
                    this@ExaminationActivity,
                    this@ExaminationActivity.supportFragmentManager,
                    this@ExaminationActivity
                ) { displayingExitScreen }
            }
        }

        fun setToolbarButtonOnClickListeners(progressBar: ProgressBar) {
            save_all_button.setOnClickListener {
                preExitScreen()

                CropEntiretySaver(
                    WeakReference(progressBar),
                    WeakReference(this),
                    onTaskFinished = this::returnToMainActivity
                )
                    .execute()
                nSavedCrops += cropBundleList.size
            }

            dismiss_all_button.setOnClickListener {
                exitActivity()
            }
        }

        fun displayDismissedImagesToastIfApplicable(nDismissedImages: Int) {
            when (nDismissedImages) {
                1 -> displayToast("Couldn't find cropping bounds for 1 image")
                in 2..Int.MAX_VALUE -> displayToast("Couldn't find cropping bounds for $nDismissedImages images")
            }
        }

        setContentView(R.layout.activity_examination).also {
            textViews = TextViews()
            toolbar = findViewById(R.id.toolbar)
        }

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

    override fun exitActivity() {
        preExitScreen()
        returnToMainActivity()
    }

    private fun preExitScreen(){
        viewPager2.removeAllViews()

        toolbar.visibility = View.GONE
        textViews.renderAppTitleVisible()
    }

    private val backPressHandler = BackPressHandler()
    /**
     * Blocked throughout the process of saving all crops,
     * otherwise asks for second one as confirmation;
     *
     * Results in return to main activity
     */
    override fun onBackPressed() {

        // block if saving all / dismissing all
        if (displayingExitScreen) {
            displayToast("Please wait until crops", "have been saved")
            return
        }

        // return to main activity if already pressed once
        else if (backPressHandler.pressedOnce) {
            return returnToMainActivity().also {
                Timber.i("Returning to main activity on second back press")
            }
        }

        backPressHandler.onPress()
        displayToast("Tap again to return to home screen")
    }

    /**
     * Clears remaining cropBundle elements contained within cropBundleList
     */
    private fun returnToMainActivity() {
        return startActivity(
            Intent(
                this,
                MainActivity::class.java
            ).putExtra(N_SAVED_CROPS, nSavedCrops)
        ).also {
            onExit()
        }
    }

    private fun onExit(){
        clearCropBundleList()
        finishAndRemoveTask()
    }
}