package com.w2sv.autocrop.activities.main.fragments.flowfield.dialogs

import android.app.Dialog
import android.os.Build
import android.os.Bundle
import android.text.SpannableStringBuilder
import androidx.core.text.italic
import com.w2sv.autocrop.R
import com.w2sv.autocrop.ui.views.UncancelableDialogFragment
import com.w2sv.autocrop.utils.android.extensions.getColoredIcon

class ScreenshotListenerDialog : UncancelableDialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        builder()
            .setTitle("Screenshot Listening")
            .setIcon(requireContext().getColoredIcon(R.drawable.ic_screenshot_24, R.color.highlight))
            .setMessage(
                SpannableStringBuilder().apply {
                    append(
                        "With screenshot listening enabled, you will get a notification whenever you take a croppable screenshot. " +
                                "Through that, you can save the crop without having to open the app." +
                                "\n\n" +
                                "For this, the "
                    )
                    italic { append("media file reading ") }
                    (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU).let { notificationPermissionRequired ->
                        if (notificationPermissionRequired) {
                            append("& ")
                            italic { append("notification posting ") }
                        }
                        append("permission")
                        append(
                            if (notificationPermissionRequired)
                                "s are "
                            else
                                " is "
                        )
                    }
                    append(
                        "required." +
                                "\n\n" +
                                "You can en-/disable screenshot listening anytime via the drawer menu."
                    )
                }
            )
            .setNegativeButton("Maybe later") { _, _ -> }
            .setPositiveButton("Enable") { _, _ -> (parentFragment as OnConfirmedListener).onConfirmed() }
            .create()

    interface OnConfirmedListener {
        fun onConfirmed()
    }
}