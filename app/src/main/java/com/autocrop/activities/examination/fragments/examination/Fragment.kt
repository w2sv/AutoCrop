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
import androidx.viewpager2.widget.ViewPager2
import com.autocrop.UserPreferences
import com.autocrop.activities.examination.fragments.ExaminationActivityFragment
import com.autocrop.activities.examination.fragments.examination.viewpager.ImageSliderAdapter
import com.autocrop.cropBundleList
import com.autocrop.retentionPercentage
import com.autocrop.utils.getByBoolean
import com.bunsenbrenner.screenshotboundremoval.R
import kotlinx.android.synthetic.main.toolbar_examination_activity.*
import timber.log.Timber
import java.util.*
import kotlin.math.roundToInt
import kotlin.properties.Delegates


interface PageDismissalImpacted {
    fun incrementNSavedCrops()
    fun exitActivity()
}


class ExaminationFragment(
    private val conductAutoScroll: Boolean,
    private val longAutoScrollDelay: Boolean
) : ExaminationActivityFragment(R.layout.activity_examination_examination),
    PageDismissalImpacted {

    private lateinit var viewPager2: ViewPager2
    private val ViewPager2.imageSliderAdapter: ImageSliderAdapter
        get() = adapter as ImageSliderAdapter
    private lateinit var textViews: TextViews
    private lateinit var toolBar: Toolbar
    private lateinit var seekBar: PageIndicationSeekBar

    inner class TextViews {
        private val retentionPercentage: TextView = findViewById(R.id.retention_percentage)
        private val pageIndication: TextView = findViewById(R.id.page_indication)

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

            with(0) {
                setPageIndication(this)
                setRetentionPercentage(this)
            }
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

        textViews = TextViews()
        toolBar = findViewById(R.id.toolbar)
        seekBar = findViewById(R.id.page_indication_seek_bar)

        initializeViewPager(textViews)
        setToolbarButtonOnClickListeners()
    }

    private fun initializeViewPager(textViews: TextViews) {
        viewPager2 = view!!.findViewById<ViewPager2>(R.id.view_pager).apply {
            adapter = ImageSliderAdapter(
                textViews,
                seekBar,
                this,
                conductAutoScroll,
                longAutoScrollDelay,
                this@ExaminationFragment,
                activity
            )

            setCurrentItem(
                imageSliderAdapter.startPosition,
                false
            )
        }
    }

    private fun setToolbarButtonOnClickListeners() {
        save_all_button.setOnClickListener {
            fun saveAll() {
                if (!viewPager2.imageSliderAdapter.scrolling)
                    return activity.invokeSaveAllFragment()
            }

            if (UserPreferences.deleteInputScreenshots) {
                class SaveAllConfirmationDialog : DialogFragment() {
                    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
                        AlertDialog.Builder(activity)
                            .run {
                                setTitle("Save all crops and delete corresponding screenshots?")
                                setNegativeButton("No") { _, _ -> }
                                setPositiveButton("Yes") { _, _ -> saveAll() }
                            }
                            .create()
                }
                SaveAllConfirmationDialog().show(
                    fragmentManager!!,
                    "Save all confirmation dialog"
                )
            } else
                saveAll()
        }

        discard_all_button.setOnClickListener {
            if (!viewPager2.imageSliderAdapter.scrolling)
                exitActivity()
        }
    }

    // -----------------ImageActionReactionsPossessor overrides-----------------

    override fun incrementNSavedCrops() {
        activity.nSavedCrops += 1
    }

    override fun exitActivity() {
        return activity.invokeAppTitleFragment(true)
    }
}


class PageIndicationSeekBar(context: Context, attr: AttributeSet) :
    AppCompatSeekBar(context, attr) {
    companion object {
        const val PERCENTAGE_TO_BE_DISPLAYED_ON_LAST_PAGE: Int = 50
    }

    private var indicateLastPage: Boolean = cropBundleList.size == 1

    init {
        progress = listOf(0, PERCENTAGE_TO_BE_DISPLAYED_ON_LAST_PAGE).getByBoolean(indicateLastPage)
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
            displayProgress(
                (progressCoefficient * pageIndex).roundToInt().also { Timber.i("Progress: $it") })
    }

    private fun displayProgress(percentage: Int) {
        with(ObjectAnimator.ofInt(this, "progress", percentage)) {
            duration = 100
            interpolator = DecelerateInterpolator()
            start()
        }
    }
}