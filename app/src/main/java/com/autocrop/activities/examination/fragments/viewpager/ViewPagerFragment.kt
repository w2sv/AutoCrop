package com.autocrop.activities.examination.fragments.viewpager

import android.os.Bundle
import android.text.SpannableStringBuilder
import android.view.View
import androidx.core.text.italic
import androidx.lifecycle.ViewModelProvider
import com.autocrop.activities.IntentIdentifier
import com.autocrop.activities.examination.fragments.ExaminationActivityFragment
import com.autocrop.uicontroller.fragment.ActivityRootFragment
import com.autocrop.utils.android.IntentExtraRetriever
import com.autocrop.utils.android.displaySnackbar
import com.autocrop.utils.numericallyInflected
import com.w2sv.autocrop.R
import com.w2sv.autocrop.databinding.ActivityExaminationFragmentViewpagerBinding

class ViewPagerFragment:
    ExaminationActivityFragment<ActivityExaminationFragmentViewpagerBinding>(),
    ActivityRootFragment {

    private lateinit var viewPagerHandler: ViewPagerHandler

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewPagerHandler = ViewPagerHandler(
            binding,
            ViewModelProvider(this)[ViewPagerFragmentViewModel::class.java],
            typedActivity
        )

        setToolbarButtonOnClickListeners()
        displayActivityEntrySnackbar()
    }

    private val nDismissedImagesRetriever = IntentExtraRetriever<Int>(IntentIdentifier.N_DISMISSED_IMAGES)

    override fun displayActivityEntrySnackbar(){
        nDismissedImagesRetriever(requireActivity().intent, 0) ?.let {
            requireActivity().displaySnackbar(
                SpannableStringBuilder()
                    .append("Couldn't find cropping bounds for ")
                    .italic { append("$it") }
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
             Triple(binding.toolbar.saveAllButton, SaveAllConfirmationDialogFragment()) {
                 with(typedActivity) {
                     replaceCurrentFragmentWith(saveAllFragment, true)
                 }
             },
            Triple(binding.toolbar.discardAllButton, DiscardAllConfirmationDialogFragment()) {
                with(typedActivity) {
                    replaceCurrentFragmentWith(appTitleFragment, false)
                }
            }
        ).forEach { (button, dialogClass, resultListener) ->
            requireActivity().supportFragmentManager.setFragmentResultListener(dialogClass.resultKey, requireActivity()){
                    _, _ -> resultListener()
            }

            button.setOnClickListener {
                if (!viewPagerHandler.scroller.isRunning)
                    dialogClass
                        .show(requireActivity().supportFragmentManager)
            }
        }
    }
}