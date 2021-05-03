/**
 * ViewPager2 Reference: https://medium.com/swlh/android-infinite-auto-image-slider-using-view-pager-2-android-studio-java-a0e450dec071
 */

package com.autocrop.activities.examination

import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.DialogFragment
import androidx.viewpager2.widget.ViewPager2
import com.autocrop.UserPreferences
import com.autocrop.activities.BackPressHandler
import com.autocrop.activities.SystemUiHidingFragmentActivity
import com.autocrop.activities.cropping.N_DISMISSED_IMAGES_IDENTIFIER
import com.autocrop.activities.examination.viewpager.ImageSliderAdapter
import com.autocrop.activities.main.MainActivity
import com.autocrop.clearCropBundleList
import com.autocrop.cropBundleList
import com.autocrop.retentionPercentage
import com.autocrop.utils.android.displayToast
import com.autocrop.utils.android.hide
import com.autocrop.utils.android.intentExtraIdentifier
import com.autocrop.utils.android.show
import com.bunsenbrenner.screenshotboundremoval.R
import kotlinx.android.synthetic.main.toolbar_examination_activity.*
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

        val appTitle: TextView = findViewById(R.id.examination_activity_title_text_view)
        val saveAll: TextView = findViewById(R.id.processing_crops_text_view)

        init {
            retentionPercentage.translationX -= (cropBundleList.size.toString().length - 1).let {
                it * 25 + it * 6
            }

            with (0){
                setPageIndication(this)
                setRetentionPercentage(this)
            }
        }

        fun setPageIndication(pageIndex: Int, itemCount: Int = cropBundleList.size){
            pageIndication.text = getString(
                R.string.fracture,
                pageIndex + 1,
                itemCount
            )
        }

        fun setRetentionPercentage(pageIndex: Int){
            retentionPercentage.text = getString(
                R.string.examination_activity_retention_percentage,
                cropBundleList[pageIndex].retentionPercentage
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        fun initializeViewPager(textViews: TextViews) {
            viewPager2 = findViewById<ViewPager2>(R.id.view_pager).apply {
                adapter = ImageSliderAdapter(
                    textViews,
                    this,
                    this@ExaminationActivity,
                    this@ExaminationActivity.supportFragmentManager,
                    this@ExaminationActivity
                ) { displayingExitScreen }

                setCurrentItem(
                    (adapter as ImageSliderAdapter).startItemIndex,
                    false
                )
            }
        }

        fun setToolbarButtonOnClickListeners(progressBar: ProgressBar) {
            save_all_button.setOnClickListener {
                fun saveAll(){
                    preExitScreen(showAppTitle = false)

                    CropEntiretySaver(
                        WeakReference(progressBar),
                        WeakReference(textViews),
                        WeakReference(this),
                        onTaskFinished = this::returnToMainActivity
                    )
                        .execute()
                    nSavedCrops += cropBundleList.size
                }

                if (UserPreferences.deleteInputScreenshots){
                    class SaveAllConfirmationDialog: DialogFragment() {
                        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog = AlertDialog.Builder(this.activity)
                            .run {
                                setTitle("Save all crops and delete corresponding screenshots?")
                                setNegativeButton("No") { _, _ -> }
                                setPositiveButton("Yes") { _, _ -> saveAll() }
                            }
                                .create()
                    }

                    SaveAllConfirmationDialog().show(supportFragmentManager, "Save all confirmation dialog")
                }

                else
                    saveAll()
            }

            dismiss_all_button.setOnClickListener {
                exitActivity()
            }
        }

        fun displayCouldntFindCroppingBoundsToast(nDismissedImages: Int){
            when (nDismissedImages) {
                1 -> displayToast("Couldn't find cropping bounds for\n1 image")
                in 2..Int.MAX_VALUE -> displayToast("Couldn't find cropping bounds for\$nDismissedImages images")
            }
        }

        setContentView(R.layout.activity_examination).also {
            textViews = TextViews()
            toolbar = findViewById(R.id.toolbar)
        }

        initializeViewPager(textViews)
        setToolbarButtonOnClickListeners(progressBar = findViewById(R.id.indeterminateBar))
        displayCouldntFindCroppingBoundsToast(
            intent.getIntExtra(
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

    private fun preExitScreen(showAppTitle: Boolean = true){
        viewPager2.removeAllViews()
        toolbar.hide()

        if (showAppTitle)
            textViews.appTitle.show()
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
            displayToast("Please wait until crops\nhave been saved")
            return
        }

        // return to main activity if already pressed once
        else if (backPressHandler.pressedOnce) {
            return returnToMainActivity()
        }

        backPressHandler.onPress()
        displayToast("Tap again to return to main screen")
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