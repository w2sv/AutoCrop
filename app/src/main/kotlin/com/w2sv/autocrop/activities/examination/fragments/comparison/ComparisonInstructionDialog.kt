package com.w2sv.autocrop.activities.examination.fragments.comparison

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.viewModels
import com.w2sv.androidutils.extensions.getColoredDrawable
import com.w2sv.androidutils.ui.UncancelableDialogFragment
import com.w2sv.autocrop.R
import com.w2sv.preferences.GlobalFlags
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@AndroidEntryPoint
internal class ComparisonInstructionDialog : UncancelableDialogFragment() {

    @HiltViewModel
    class ViewModel @Inject constructor(val globalFlags: GlobalFlags) : androidx.lifecycle.ViewModel()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        builder()
            .apply {
                setTitle("Comparison Screen")
                setIcon(
                    context.getColoredDrawable(
                        R.drawable.ic_crop_original_24,
                        com.w2sv.common.R.color.magenta_saturated
                    )
                )
                setMessage(getString(R.string.comparison_instruction))
                setPositiveButton(resources.getString(R.string.got_it)) { _, _ ->
                    viewModels<ViewModel>().value.globalFlags.comparisonInstructionsShown = true
                }
            }
            .create()
}