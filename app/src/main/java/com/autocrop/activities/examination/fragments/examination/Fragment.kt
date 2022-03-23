package com.autocrop.activities.examination.fragments.examination

import android.animation.ObjectAnimator
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.AttributeSet
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.TextView
import androidx.appcompat.widget.AppCompatSeekBar
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.viewpager2.widget.ViewPager2
import com.autocrop.UserPreferences
import com.autocrop.activities.examination.ExaminationViewModel
import com.autocrop.activities.examination.fragments.ExaminationActivityFragment
import com.autocrop.activities.examination.fragments.examination.viewpager.ViewPagerHandler
import com.autocrop.cropBundleList
import com.autocrop.retentionPercentage
import com.autocrop.utils.get
import com.w2sv.autocrop.R
import kotlinx.android.synthetic.main.toolbar_examination_activity.*
import timber.log.Timber
import kotlin.math.roundToInt
import kotlin.properties.Delegates


class ExaminationFragment() : ExaminationActivityFragment(R.layout.activity_examination_examination){

    private val viewModel: ExaminationViewModel by activityViewModels<ExaminationViewModel>()

    private lateinit var viewPagerHandler: ViewPagerHandler

    private lateinit var textViews: TextViews
    private lateinit var toolBar: Toolbar
    private lateinit var seekBar: PageIndicationSeekBar

    inner class TextViews(private val retentionPercentage: TextView, private val pageIndication: TextView) {
        /**
         * Adjusts x translation of retentionPercentage view wrt initial
         * length of page indication view text
         *
         * Initializes page dependent texts
         */
        init {
            retentionPercentage.translationX -= (cropBundleList.size.toString().lastIndex) * 31

            setPageIndication(0)
            setRetentionPercentage(0)
        }

        fun setPageIndication(pageIndex: Int, itemCount: Int = cropBundleList.size) {
            pageIndication.text = getString(
                R.string.fracture,
                pageIndex + 1,
                itemCount
            )
        }

        fun setRetentionPercentage(pageIndex: Int) {
            retentionPercentage.text = getString(
                R.string.examination_activity_retention_percentage,
                cropBundleList[pageIndex].retentionPercentage
            )
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        textViews = TextViews(findViewById(R.id.retention_percentage), findViewById(R.id.page_indication))
        toolBar = findViewById(R.id.toolbar)
        seekBar = findViewById(R.id.page_indication_seek_bar)

        viewPagerHandler = ViewPagerHandler(
            view.findViewById<ViewPager2>(R.id.view_pager),
            examinationActivity,
            viewModel,
            textViews,
            seekBar,
        ){ examinationActivity.saveAllFragment.value.invoke(false) }
        setToolbarButtonOnClickListeners()
    }

    /**
     * Runs defined onClickListeners only if scroller not running
     */
    private fun setToolbarButtonOnClickListeners() {

        fun runIfScrollerNotRunning(f: () -> Unit){
            if (viewPagerHandler.scroller.run {this == null || !this.isRunning })
                f()
        }

        fun showConfirmationDialog(title: String, positiveButtonClickListenerAction: () -> Unit){
            object: DialogFragment() {
                override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
                    AlertDialog.Builder(activity)
                        .run {
                            setTitle(title)
                            setNegativeButton("No") { _, _ -> }
                            setPositiveButton("Yes") { _, _ -> positiveButtonClickListenerAction() }
                        }
                        .create()
            }.show(parentFragmentManager,"")
        }

        // ------------save_all_button
        save_all_button.setOnClickListener {
            runIfScrollerNotRunning {
                showConfirmationDialog("Save all crops${listOf("", " and delete corresponding screenshots")[UserPreferences.deleteInputScreenshots]}?") {
                    examinationActivity.saveAllFragment.value.invoke(true)
                }
            }
        }

        // --------------discard_all_button
        discard_all_button.setOnClickListener {
            runIfScrollerNotRunning {
                showConfirmationDialog("Discard all crops?") {
                    examinationActivity.saveAllFragment.value.invoke(false)
                }
            }
        }
    }
}


class PageIndicationSeekBar(context: Context, attr: AttributeSet) : AppCompatSeekBar(context, attr) {

    companion object {
        const val PERCENTAGE_TO_BE_DISPLAYED_ON_LAST_PAGE: Int = 50
    }

    private var indicateLastPage: Boolean = cropBundleList.size == 1

    init {
        progress = listOf(0, PERCENTAGE_TO_BE_DISPLAYED_ON_LAST_PAGE)[indicateLastPage]
        isEnabled = false
    }

    private var progressCoefficient by Delegates.notNull<Float>()

    fun calculateProgressCoefficient(dataMagnitude: Int = cropBundleList.size) {
        if (dataMagnitude == 1)
            indicateLastPage = true.also {
                displayProgress(PERCENTAGE_TO_BE_DISPLAYED_ON_LAST_PAGE)
            }
        else
            progressCoefficient = max.toFloat() / dataMagnitude.minus(1).toFloat()
    }

    fun indicatePage(pageIndex: Int) {
        if (!indicateLastPage)
            displayProgress((progressCoefficient * pageIndex).roundToInt())
    }

    private fun displayProgress(percentage: Int) {
        with(ObjectAnimator.ofInt(this, "progress", percentage)) {
            duration = 100
            interpolator = DecelerateInterpolator()
            start()
        }
    }
}