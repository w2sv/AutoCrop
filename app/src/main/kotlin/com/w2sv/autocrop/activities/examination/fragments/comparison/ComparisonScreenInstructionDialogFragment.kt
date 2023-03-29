package com.w2sv.autocrop.activities.examination.fragments.comparison

import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.viewModels
import com.w2sv.androidutils.extensions.getColoredDrawable
import com.w2sv.autocrop.R
import com.w2sv.autocrop.ui.views.RoundedDialogFragment
import com.w2sv.common.preferences.GlobalFlags
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@AndroidEntryPoint
internal class ComparisonScreenInstructionDialogFragment : RoundedDialogFragment() {

    @HiltViewModel
    class ViewModel @Inject constructor(val globalFlags: GlobalFlags) : androidx.lifecycle.ViewModel()

    override fun AlertDialog.Builder.build(): AlertDialog.Builder =
        apply {
            setCancelable(false)
            setTitle("Comparison Screen")
            setIcon(
                context.getColoredDrawable(
                    R.drawable.ic_crop_original_24,
                    com.w2sv.common.R.color.magenta_saturated
                )
            )
            setMessage(getString(R.string.comparison_instruction))
            setPositiveButton(resources.getString(R.string.got_it))
            { _, _ ->
                viewModels<ViewModel>().value.globalFlags.comparisonInstructionsShown = true
            }
        }
}