package com.autocrop.activities.examination.fragments.viewpager

import android.os.Bundle
import android.text.SpannableStringBuilder
import android.view.View
import androidx.core.text.bold
import androidx.core.text.color
import androidx.lifecycle.ViewModelProvider
import com.autocrop.activities.IntentIdentifier
import com.autocrop.activities.examination.fragments.ExaminationActivityFragment
import com.autocrop.activities.examination.fragments.viewpager.dialogs.DiscardAllConfirmationDialog
import com.autocrop.activities.examination.fragments.viewpager.dialogs.SaveAllConfirmationDialog
import com.autocrop.uicontroller.fragment.ActivityRootFragment
import com.autocrop.utils.android.IntentExtraRetriever
import com.autocrop.utils.android.displaySnackbar
import com.autocrop.utils.android.getColorInt
import com.autocrop.utils.numericallyInflected
import com.w2sv.autocrop.R
import com.w2sv.autocrop.databinding.ActivityExaminationFragmentViewpagerBinding

class ViewPagerFragment:
    ExaminationActivityFragment<ActivityExaminationFragmentViewpagerBinding>(),
    ActivityRootFragment {

    companion object{
        private const val CURRENT_VIEW_PAGER_POSITION = "CURRENT_VIEW_PAGER_POSITION"
    }

    private lateinit var viewPagerHandler: ViewPagerHandler
    private lateinit var viewModel: ViewPagerFragmentViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this)[ViewPagerFragmentViewModel::class.java]

        viewPagerHandler = ViewPagerHandler(
            binding,
            viewModel,
            typedActivity,
            savedInstanceState?.getInt(CURRENT_VIEW_PAGER_POSITION)
        )

        setToolbarButtonOnClickListeners()

        if (savedInstanceState == null)
            displayActivityEntrySnackbar()
    }

    private val nDismissedImagesRetriever = IntentExtraRetriever<Int>(IntentIdentifier.N_DISMISSED_IMAGES)

    override fun displayActivityEntrySnackbar(){
        nDismissedImagesRetriever(requireActivity().intent, 0) ?.let {
            requireActivity().displaySnackbar(
                SpannableStringBuilder()
                    .append("Couldn't find cropping bounds for ")
                    .bold {
                        color(
                            getColorInt(R.color.saturated_magenta, requireContext())
                        ) { append("$it") }
                    }
                    .append(" image".numericallyInflected(it)),
                R.drawable.ic_error_24
            )
        }
    }

    /**
     * Runs defined onClickListeners only if scroller not running
     */
    private fun setToolbarButtonOnClickListeners() {
        arrayOf(
             Triple(binding.saveAllButton, SaveAllConfirmationDialog()) {
                 with(typedActivity) {
                     replaceCurrentFragmentWith(saveAllFragment, true)
                 }
             },
            Triple(binding.discardAllButton, DiscardAllConfirmationDialog()) {
                with(typedActivity) {
                    replaceCurrentFragmentWith(appTitleFragment, false)
                }
            }
        )
            .forEach { (button, dialogClass, resultListener) ->
                requireActivity().supportFragmentManager.setFragmentResultListener(dialogClass.resultKey, requireActivity()){
                        _, _ -> resultListener()
                }

                button.setOnClickListener {
                    if (!viewModel.autoScroll)
                        dialogClass
                            .show(requireActivity().supportFragmentManager)
                }
            }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putInt(CURRENT_VIEW_PAGER_POSITION, binding.viewPager.currentItem)
    }
}
