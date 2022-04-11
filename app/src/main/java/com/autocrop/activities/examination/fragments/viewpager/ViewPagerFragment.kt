package com.autocrop.activities.examination.fragments.viewpager

import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import com.autocrop.activities.examination.fragments.ExaminationActivityFragment
import com.w2sv.autocrop.databinding.ActivityExaminationFragmentViewpagerBinding

class ViewPagerFragment
    : ExaminationActivityFragment<ActivityExaminationFragmentViewpagerBinding>(){

    private lateinit var viewPagerHandler: ViewPagerHandler

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewPagerHandler = ViewPagerHandler(
            binding,
            ViewModelProvider(this)[ViewPagerFragmentViewModel::class.java],
            typedActivity
        )

        setToolbarButtonOnClickListeners()
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