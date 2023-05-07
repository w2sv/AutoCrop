package com.w2sv.autocrop.activities.examination.fragments.comparison

import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.viewModels
import com.w2sv.androidutils.ui.resources.getColoredDrawable
import com.w2sv.autocrop.R
import com.w2sv.autocrop.ui.views.RoundedDialogFragment
import com.w2sv.common.datastore.Repository
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@AndroidEntryPoint
internal class ComparisonScreenInstructionDialogFragment : RoundedDialogFragment() {

    @HiltViewModel
    class ViewModel @Inject constructor(val repository: Repository) : androidx.lifecycle.ViewModel() {

        fun onDismissDialog() {
            repository.comparisonInstructionsShown.value = true
        }
    }

    private val viewModel by viewModels<ViewModel>()

    override fun AlertDialog.Builder.build(): AlertDialog.Builder =
        apply {
            setTitle(getString(R.string.comparison_screen))
            setIcon(
                context.getColoredDrawable(
                    R.drawable.ic_crop_original_24,
                    com.w2sv.common.R.color.magenta_saturated
                )
            )
            setMessage(getString(R.string.comparison_instruction))
            setPositiveButton(resources.getString(R.string.got_it)) { _, _ ->
                viewModel.onDismissDialog()
            }
            setOnDismissListener { viewModel.onDismissDialog() }  // TODO
        }
}