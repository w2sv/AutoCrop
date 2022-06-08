package com.autocrop.activities.examination.fragments.viewpager.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.text.SpannableStringBuilder
import androidx.core.text.bold
import androidx.core.text.color
import androidx.core.text.italic
import com.autocrop.uielements.ExtendedDialogFragment
import com.autocrop.utils.BlankFun
import com.autocrop.utilsandroid.getColoredIcon
import com.autocrop.utilsandroid.getThemedColor
import com.w2sv.autocrop.R

class InstructionsDialog: ExtendedDialogFragment(){
    init {
        isCancelable = false
    }

    var positiveButtonOnClickListener: BlankFun? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        AlertDialog.Builder(requireContext())
            .apply {
                setTitle("Some instructions to get cha goin'")
                setIcon(context.getColoredIcon(R.drawable.ic_outline_info_24, R.color.accentuated_tv))

                setMessage(
                    SpannableStringBuilder()
                        .color(context.getThemedColor(R.color.accentuated_tv)){append("•")}
                        .append(" Tap screen once to save/delete")
                        .bold { append(" current") }
                        .append(" crop \uD83D\uDC47\n\n")
                        .color(context.getThemedColor(R.color.accentuated_tv)){append("•")}
                        .append(" Tap screen and hold to save/delete")
                        .bold { append(" all") }
                        .append(" crops 👇⏳")
                )
                setCancelable(false)

                setPositiveButton("Got it!") { _, _ -> positiveButtonOnClickListener?.invoke() }
            }
            .create()
}