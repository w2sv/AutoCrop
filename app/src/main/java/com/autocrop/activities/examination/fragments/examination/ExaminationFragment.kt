package com.autocrop.activities.examination.fragments.examination

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.autocrop.UserPreferences
import com.autocrop.activities.examination.ExaminationViewModel
import com.autocrop.activities.examination.fragments.ExaminationActivityFragment
import com.autocrop.activities.examination.fragments.examination.viewpager.ViewPagerHandler
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

        fun showConfirmationDialogIfScrollerNotRunning(title: String, positiveButtonOnClickListener: () -> Unit) {
            if (!viewPagerHandler.scroller.isRunning)
                CropEntiretyActionConfirmationDialogFragment(title, positiveButtonOnClickListener)
                    .show(childFragmentManager, CropEntiretyActionConfirmationDialogFragment.TAG)
        }

        // ------------save_all_button
        binding.toolbar.saveAllButton.setOnClickListener {
            showConfirmationDialogIfScrollerNotRunning(
                "Save all crops${listOf("", " and delete corresponding screenshots")[UserPreferences.deleteInputScreenshots]}?") {
                with(activity){saveAllFragment.commit(true)}
            }
        }

        // --------------discard_all_button
        binding.toolbar.discardAllButton.setOnClickListener {
            showConfirmationDialogIfScrollerNotRunning("Discard all crops?") {
                with(activity){appTitleFragment.commit(false)}
            }
        }
    }

}

class CropEntiretyActionConfirmationDialogFragment(
    private val title: String,
    private val positiveButtonOnClickListener: () -> Unit)
        : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        AlertDialog.Builder(requireContext())
            .run {
                setTitle(title)
                setNegativeButton("No") { _, _ -> }
                setPositiveButton("Yes") { _, _ -> positiveButtonOnClickListener() }
            }
            .create()

    companion object {
        val TAG = ::CropEntiretyActionConfirmationDialogFragment.javaClass.name
    }
}
