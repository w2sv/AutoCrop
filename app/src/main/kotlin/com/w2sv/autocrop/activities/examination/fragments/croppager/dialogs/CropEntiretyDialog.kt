package com.w2sv.autocrop.activities.examination.fragments.croppager.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.text.SpannableStringBuilder
import androidx.core.text.color
import androidx.core.text.italic
import com.w2sv.androidutils.extensions.getThemedColor
import com.w2sv.autocrop.R

class CropEntiretyDialog : AbstractCropDialog() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        AlertDialog.Builder(requireContext())
            .apply {
                setTitle(
                    SpannableStringBuilder()
                        .append("Save ")
                        .color(context.getThemedColor(R.color.highlight)) {
                            italic {
                                append("all ")
                            }
                        }
                        .append("crops?")
                )
                setDeleteCorrespondingScreenshotsOption("Delete corresponding screenshots")
                setNegativeButton("No, discard all") { _, _ -> notifyResultListener(false) }
                setPositiveButton("Yes") { _, _ -> notifyResultListener(true) }
            }
            .create()

    interface ResultListener {
        fun onCropEntiretyDialogResult(confirmed: Boolean)
    }

    private fun notifyResultListener(confirmed: Boolean) {
        (parentFragment as ResultListener)
            .onCropEntiretyDialogResult(confirmed)
    }
}