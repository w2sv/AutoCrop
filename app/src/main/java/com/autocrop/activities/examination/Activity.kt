/**
 * ViewPager2 Reference: https://medium.com/swlh/android-infinite-auto-image-slider-using-view-pager-2-android-studio-java-a0e450dec071
 */

package com.autocrop.activities.examination

import android.animation.ObjectAnimator
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.AttributeSet
import android.view.animation.DecelerateInterpolator
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.widget.AppCompatSeekBar
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
import com.autocrop.utils.android.*
import com.autocrop.utils.toInt
import com.blogspot.atifsoftwares.animatoolib.Animatoo
import com.bunsenbrenner.screenshotboundremoval.R
import kotlinx.android.synthetic.main.toolbar_examination_activity.*
import timber.log.Timber
import java.lang.ref.WeakReference
import kotlin.math.roundToInt
import kotlin.properties.Delegates


val N_SAVED_CROPS: String = intentExtraIdentifier("n_saved_crops")


interface ImageActionReactionsPossessor {
    fun incrementNSavedCrops()
    fun exitActivity()
}


class PageIndicationSeekBar(context: Context, attr: AttributeSet): AppCompatSeekBar(context, attr){
    companion object{
        const val PERCENTAGE_TO_BE_DISPLAYED_ON_LAST_PAGE: Int = 50
    }

    private var indicateLastPage: Boolean = cropBundleList.size == 1

    init{
        progress = listOf(0, PERCENTAGE_TO_BE_DISPLAYED_ON_LAST_PAGE)[indicateLastPage.toInt()]
        isEnabled = false
    }

    private var progressCoefficient by Delegates.notNull<Float>()

    fun calculateProgressCoefficient(dataMagnitude: Int = cropBundleList.size){
        if (dataMagnitude == 1)
            indicateLastPage = true.also {
                displayProgress(PERCENTAGE_TO_BE_DISPLAYED_ON_LAST_PAGE)
            }
        else
            progressCoefficient = max.toFloat() / dataMagnitude.minus(1).toFloat()
    }

    fun indicatePage(pageIndex: Int){
        if (!indicateLastPage)
            displayProgress((progressCoefficient * pageIndex).roundToInt().also { Timber.i("Progress: $it") })
    }

    private fun displayProgress(percentage: Int){
        with(ObjectAnimator.ofInt(this, "progress", percentage)){
            duration = 100
            interpolator = DecelerateInterpolator()
            start()
        }
    }
}


class ExaminationActivity : SystemUiHidingFragmentActivity(), ImageActionReactionsPossessor {
    private lateinit var viewPager2: ViewPager2
    private lateinit var textViews: TextViews
    private lateinit var toolBar: Toolbar
    private lateinit var seekBar: PageIndicationSeekBar

    private var nSavedCrops: Int = 0
    private var displayingExitScreen: Boolean = false
    private var savingAll: Boolean = false

    inner class TextViews {
        private val retentionPercentage: TextView = findViewById(R.id.retention_percentage)
        private val pageIndication: TextView = findViewById(R.id.page_indication)

        val appTitle: TextView = findViewById(R.id.examination_activity_title_text_view)
        val saveAll: TextView = findViewById(R.id.processing_crops_text_view)

        /**
         * Adjusts x translation of retentionPercentage view wrt initial
         * length of page indication view text
         *
         * Initializes page dependent texts
         */
        init {
            retentionPercentage.translationX -= (cropBundleList.size.toString().lastIndex).let {
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
                    seekBar,
                    this,
                    this@ExaminationActivity,
                    this@ExaminationActivity.supportFragmentManager,
                    this@ExaminationActivity
                )

                setCurrentItem(
                    (adapter as ImageSliderAdapter).startItemIndex,
                    false
                )
            }
        }

        fun setToolbarButtonOnClickListeners(progressBar: ProgressBar) {
            save_all_button.setOnClickListener {
                fun saveAll(){
                    savingAll = true
                    preExitScreen(showAppTitle = false)

                    CropSaver(
                        WeakReference(progressBar),
                        WeakReference(textViews),
                        WeakReference(this),
                        onTaskFinished = ::returnToMainActivity
                    )
                        .execute()
                    nSavedCrops += cropBundleList.size
                }

                if (UserPreferences.deleteInputScreenshots){
                    class SaveAllConfirmationDialog: DialogFragment() {
                        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog = AlertDialog.Builder(activity)
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

        fun displayCropDismissalToast(nDismissedImages: Int){
            with(R.color.saturated_magenta){
                when (nDismissedImages) {
                    1 -> displaySnackbar("Couldn't find cropping bounds for\n1 image", this)
                    in 2..Int.MAX_VALUE -> displaySnackbar("Couldn't find cropping bounds for\n$nDismissedImages images", this)
                }
            }
        }

        setContentView(R.layout.activity_examination).also {
            textViews = TextViews()
            toolBar = findViewById(R.id.toolbar)
            seekBar = findViewById(R.id.page_indication_seek_bar)
        }

        initializeViewPager(textViews)
        setToolbarButtonOnClickListeners(progressBar = findViewById(R.id.indeterminateBar))
        with(intent.getIntExtra(N_DISMISSED_IMAGES_IDENTIFIER, 0)){
            if (!equals(0))
                displayCropDismissalToast(this)
                    .also {
                        intent.removeExtra(N_DISMISSED_IMAGES_IDENTIFIER)
                    }
        }
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
        displayingExitScreen = true

        viewPager2.removeAllViews()
        toolBar.hide()
        seekBar.hide()

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
            if (savingAll)
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
                if (displayingExitScreen)
                    restartTransitionAnimation()
                else
                    proceedTransitionAnimation()
            onExit()
        }
    }

    private fun onExit(){
        clearCropBundleList()
        finishAndRemoveTask()
    }
}