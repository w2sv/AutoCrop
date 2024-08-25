package com.w2sv.autocrop.ui.screen.comparison

import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.viewModels
import androidx.lifecycle.viewModelScope
import com.w2sv.androidutils.graphics.getColoredDrawable
import com.w2sv.autocrop.R
import com.w2sv.autocrop.ui.views.RoundedDialogFragment
import com.w2sv.domain.repository.PreferencesRepository
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
internal class ComparisonScreenInstructionDialogFragment : RoundedDialogFragment() {

    @HiltViewModel
    class ViewModel @Inject constructor(private val preferencesRepository: PreferencesRepository) : androidx.lifecycle.ViewModel() {

        fun onDismissDialog() {
            viewModelScope.launch { preferencesRepository.comparisonInstructionsShown.save(true) }
        }
    }

    private val viewModel by viewModels<ViewModel>()

    override fun AlertDialog.Builder.build(): AlertDialog.Builder =
        apply {
            setTitle(getString(R.string.comparison_screen))
            setIcon(
                context.getColoredDrawable(
                    R.drawable.ic_inspect_image_24,
                    com.w2sv.core.common.R.color.magenta_saturated
                )
            )
            setMessage(getString(R.string.comparison_instruction))
            setPositiveButton(resources.getString(R.string.got_it)) { _, _ ->
                viewModel.onDismissDialog()
            }
            setOnDismissListener { viewModel.onDismissDialog() }  // TODO
        }
}