package com.autocrop.activities.iodetermination.fragments.croppager.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.text.SpannableStringBuilder
import androidx.core.text.bold
import androidx.core.text.color
import com.autocrop.ui.elements.ExtendedDialogFragment
import com.autocrop.utils.android.extensions.getColoredIcon
import com.autocrop.utils.android.extensions.getThemedColor
import com.autocrop.utils.kotlin.BlankFun
import com.w2sv.autocrop.R

class InstructionsDialog: ExtendedDialogFragment(){
    init { isCancelable = false }

    var positiveButtonOnClickListener: BlankFun? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        AlertDialog.Builder(requireContext())
            .apply {
                setCancelable(false)
                setTitle("Some instructions to get ya going")
                setIcon(context.getColoredIcon(R.drawable.ic_outline_info_24, R.color.highlighted_text_view))
                setMessage(
                    SpannableStringBuilder()
                        .addFormattedInstruction("Tap screen once to save/discard", "current", "crop \uD83D\uDC47")
                        .append("\n\n")
                        .addFormattedInstruction("Tap screen and hold to save/discard", "all", "crops \uD83D\uDC47⏳")
                )
                setPositiveButton("Got it!") { _, _ -> positiveButtonOnClickListener?.invoke() }
            }
            .create()

    private fun SpannableStringBuilder.addFormattedInstruction(start: String, bold: String, end: String): SpannableStringBuilder =
        apply {
            color(requireContext().getThemedColor(R.color.highlighted_text_view)){append("•")}
            append(" $start")
            bold { append(" $bold") }
            append(" $end")
        }
}