package com.autocrop.activities.examination.fragments.examination

import android.animation.ObjectAnimator
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.TextView
import androidx.appcompat.widget.AppCompatSeekBar
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import com.autocrop.UserPreferences
import com.autocrop.activities.examination.ExaminationActivity
import com.autocrop.activities.examination.ExaminationViewModel
import com.autocrop.activities.examination.PageIndicationSeekBarModel
import com.autocrop.activities.examination.fragments.ExaminationActivityFragment
import com.autocrop.activities.examination.fragments.examination.viewpager.ViewPagerHandler
import com.autocrop.cropBundleList
import com.autocrop.retentionPercentage
import com.autocrop.utils.get
import com.w2sv.autocrop.R
import com.w2sv.autocrop.databinding.ActivityExaminationFragmentRootBinding

class ExaminationFragment() : ExaminationActivityFragment(R.layout.activity_examination_fragment_root){

    private val viewModel: ExaminationViewModel by activityViewModels<ExaminationViewModel>()

    private var _binding: ActivityExaminationFragmentRootBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewPagerHandler: ViewPagerHandler

    private lateinit var textViews: TextViews

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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = ActivityExaminationFragmentRootBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        textViews = TextViews(findViewById(R.id.retention_percentage), findViewById(R.id.page_indication))

        viewPagerHandler = ViewPagerHandler(
            binding.viewPager,
            examinationActivity,
            textViews,
            binding.pageIndicationSeekBar,
        ){ examinationActivity.appTitleFragment.value.invoke(false) }
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
        binding.toolbar.saveAllButton.setOnClickListener {
            runIfScrollerNotRunning {
                showConfirmationDialog("Save all crops${listOf("", " and delete corresponding screenshots")[UserPreferences.deleteInputScreenshots]}?") {
                    examinationActivity.saveAllFragment.value.invoke(true)
                }
            }
        }

        // --------------discard_all_button
        binding.toolbar.discardAllButton.setOnClickListener {
            runIfScrollerNotRunning {
                showConfirmationDialog("Discard all crops?") {
                    examinationActivity.saveAllFragment.value.invoke(false)
                }
            }
        }
    }
}

class PageIndicationSeekBar(context: Context, attr: AttributeSet) : AppCompatSeekBar(context, attr) {
    private val viewModel: PageIndicationSeekBarModel by lazy {
        ViewModelProvider(context as ExaminationActivity)[ExaminationViewModel::class.java].viewPager.pageIndicationSeekBar
    }

    init {
        progress = viewModel.pagePercentage(0, max)
        isEnabled = false  // disables dragging of bar
    }

    fun displayPage(pageIndex: Int) {
        with(ObjectAnimator.ofInt(this, "progress", viewModel.pagePercentage(pageIndex, max))) {
            duration = 100
            interpolator = DecelerateInterpolator()
            start()
        }
    }
}