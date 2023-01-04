package com.w2sv.autocrop.activities.main.fragments.flowfield.dialogs

import android.app.Dialog
import android.os.Build
import android.os.Bundle
import android.text.SpannableStringBuilder
import androidx.core.text.italic
import com.w2sv.androidutils.extensions.getColoredIcon
import com.w2sv.androidutils.ui.UncancelableDialogFragment
import com.w2sv.autocrop.R

class ScreenshotListenerDialog : UncancelableDialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        builder()
            .setTitle("Screenshot Listening")
            .setIcon(requireContext().getColoredIcon(R.drawable.ic_hearing_24, R.color.highlight))
            .setMessage(
                SpannableStringBuilder().apply {
                    append(
                        "With screenshot listening enabled, you will get a notification whenever you take a croppable screenshot. " +
                                "Through that, you can save the crop without having to open the app." +
                                "\n\n" +
                                "For this, the "
                    )
                    italic { append("media file reading ") }
                    val notificationPermissionRequired = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
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
                    append(
                        "required." +
                                "\n\n" +
                                "You can en-/disable screenshot listening anytime via the drawer menu."
                    )
                }
            )
            .setNegativeButton("Maybe later") { _, _ ->
                (parentFragment as Listener)
                    .onScreenshotListenerDialogAnsweredListener()
            }
            .setPositiveButton("Enable") { _, _ ->
                with(parentFragment as Listener) {
                    onScreenshotListenerDialogConfirmedListener()
                    onScreenshotListenerDialogAnsweredListener()
                }
            }
            .create()

    interface Listener {
        fun onScreenshotListenerDialogConfirmedListener()
        fun onScreenshotListenerDialogAnsweredListener()
    }
}