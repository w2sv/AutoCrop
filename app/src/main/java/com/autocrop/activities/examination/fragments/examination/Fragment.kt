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
import com.autocrop.activities.examination.ViewPagerModel
import com.autocrop.activities.examination.fragments.ExaminationActivityFragment
import com.autocrop.activities.examination.fragments.examination.viewpager.ViewPagerHandler
import com.autocrop.retentionPercentage
import com.autocrop.utils.get
import com.w2sv.autocrop.R
import com.w2sv.autocrop.databinding.ActivityExaminationFragmentRootBinding

class ExaminationFragment : ExaminationActivityFragment(R.layout.activity_examination_fragment_root){

    private val viewModel: ExaminationViewModel by activityViewModels<ExaminationViewModel>()

    private lateinit var viewPagerHandler: ViewPagerHandler

    protected var _binding: ActivityExaminationFragmentRootBinding? = null
    protected val binding: ActivityExaminationFragmentRootBinding
        get() = _binding!!

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

        viewPagerHandler = ViewPagerHandler(binding)
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

interface ViewModelRetriever{
    abstract val viewModel: ViewPagerModel
}

class ViewModelRetrieverImplementation(context: Context): ViewModelRetriever{
    override val viewModel: ViewPagerModel by lazy {
        ViewModelProvider(context as ExaminationActivity)[ExaminationViewModel::class.java].viewPager
    }
}

class PageIndicationSeekBar(context: Context, attr: AttributeSet) :
    AppCompatSeekBar(context, attr),
    ViewModelRetriever by ViewModelRetrieverImplementation(context)
{
    init {
        progress = viewModel.pageIndicationSeekBar.pagePercentage(0, max)
        isEnabled = false  // disables dragging of bar
    }

    fun update(dataSetPosition: Int) {
        with(ObjectAnimator.ofInt(this, "progress", viewModel.pageIndicationSeekBar.pagePercentage(dataSetPosition, max))) {
            duration = 100
            interpolator = DecelerateInterpolator()
            start()
        }
    }
}

abstract class PageDependentTextView(context: Context, attr: AttributeSet, private val stringId: Int):
    TextView(context, attr),
    ViewModelRetriever by ViewModelRetrieverImplementation(context)
{
    fun updateText(viewPagerDataSetPosition: Int){
        text = context.resources.getString(stringId, *formatArgs(viewPagerDataSetPosition).toTypedArray())
    }
    protected abstract fun formatArgs(viewPagerDataSetPosition: Int): List<Int>
}

class CropRetentionPercentageTextView(context: Context, attr: AttributeSet): PageDependentTextView(context, attr, R.string.examination_activity_retention_percentage){
    init{
        translationX -= (viewModel.dataSet.size.toString().lastIndex) * 31
        updateText(0)
    }
    override fun formatArgs(viewPagerDataSetPosition: Int): List<Int> = listOf(viewModel.dataSet[viewPagerDataSetPosition].retentionPercentage)
}

class PageIndicationTextView(context: Context, attr: AttributeSet): PageDependentTextView(context, attr, R.string.fracture){
    override fun formatArgs(viewPagerDataSetPosition: Int): List<Int> = listOf(viewModel.dataSet.pageIndex(viewPagerDataSetPosition) + 1, viewModel.dataSet.size)
}