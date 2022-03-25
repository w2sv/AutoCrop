package com.autocrop.activities.examination.fragments.viewpager

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.autocrop.UserPreferences
import com.autocrop.activities.examination.fragments.ExaminationActivityFragment
import com.w2sv.autocrop.R
import com.w2sv.autocrop.databinding.ActivityExaminationFragmentViewpagerBinding

class ViewPagerFragment : ExaminationActivityFragment(R.layout.activity_examination_fragment_viewpager){

    private lateinit var viewPagerHandler: ViewPagerHandler

    protected var _binding: ActivityExaminationFragmentViewpagerBinding? = null
    protected val binding: ActivityExaminationFragmentViewpagerBinding
        get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = ActivityExaminationFragmentViewpagerBinding.inflate(inflater, container, false)
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

        fun ifScrollerNotRunning(f: () -> Unit){
            if (!viewPagerHandler.scroller.isRunning)
                f()
        }

        // ------------save_all_button
        binding.toolbar.saveAllButton.setOnClickListener {
            ifScrollerNotRunning {
                CropEntiretyActionConfirmationDialogFragment("Save all crops?", true)
                {
                    with(activity){
                        saveAllFragment.commit(true)
                    }
                }
                    .show(childFragmentManager, "SAVE_ALL_BUTTON_CONFIRMATION_DIALOG")
            }
        }

        // --------------discard_all_button
        binding.toolbar.discardAllButton.setOnClickListener {
            ifScrollerNotRunning {
                CropEntiretyActionConfirmationDialogFragment("Discard all crops?", false)
                { with(activity){appTitleFragment.commit(false)} }
                    .show(childFragmentManager, "DISCARD_ALL_BUTTON_CONFIRMATION_DIALOG")
            }
        }
    }
}

class CropEntiretyActionConfirmationDialogFragment(
    private val title: String,
    private val addDeleteCorrespondingScreenshotsCheckbox: Boolean,
    private val positiveButtonOnClickListener: () -> Unit)
        : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        AlertDialog.Builder(requireContext())
            .run {
                setTitle(title)
                if (addDeleteCorrespondingScreenshotsCheckbox)
                    setMultiChoiceItems(arrayOf("Delete corresponding screenshots"), booleanArrayOf(UserPreferences.deleteScreenshotsOnSaveAll)){_, _, _ -> UserPreferences.toggle(UserPreferences.Keys.deleteScreenshotsOnSaveAll)}
                setNegativeButton("No") { _, _ -> }
                setPositiveButton("Yes") { _, _ -> positiveButtonOnClickListener() }
            }
            .create()
}
