package com.w2sv.autocrop.activities.cropexamination.fragments.croppager.dialogs

import android.app.Dialog
import android.os.Bundle
import android.text.SpannableStringBuilder
import androidx.core.text.bold
import androidx.core.text.color
import com.w2sv.androidutils.extensions.getColoredIcon
import com.w2sv.androidutils.extensions.getThemedColor
import com.w2sv.androidutils.ui.UncancelableDialogFragment
import com.w2sv.autocrop.R

class CropPagerInstructionsDialog : UncancelableDialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        builder()
            .setTitle("Some instructions to get ya going")
            .setIcon(requireContext().getColoredIcon(R.drawable.ic_outline_info_24, R.color.highlight))
            .setMessage(
                SpannableStringBuilder()
                    .addFormattedInstruction("Tap screen once to save/discard", "current", "crop \uD83D\uDC47")
                    .append("\n\n")
                    .addFormattedInstruction("Tap screen and hold to save/discard", "all", "crops \uD83D\uDC47⏳")
            )
            .setPositiveButton("Got it!") { _, _ ->
                (requireParentFragment() as OnDismissedListener)
                    .onDismissed()
            }
            .create()

    private fun SpannableStringBuilder.addFormattedInstruction(
        start: String,
        highlighted: String,
        end: String
    ): SpannableStringBuilder =
        apply {
            color(requireContext().getThemedColor(R.color.magenta_bright)) { append("•") }
            append(" $start")
            bold { append(" $highlighted") }
            append(" $end")
        }

    interface OnDismissedListener {
        fun onDismissed()
    }
}