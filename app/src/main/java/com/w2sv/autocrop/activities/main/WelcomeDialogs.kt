package com.w2sv.autocrop.activities.main

import android.app.Dialog
import android.content.DialogInterface
import android.os.Build
import android.os.Bundle
import android.text.SpannableStringBuilder
import androidx.core.text.italic
import com.w2sv.autocrop.R
import com.w2sv.autocrop.ui.views.UncancelableDialogFragment
import com.w2sv.autocrop.utils.android.extensions.getColoredIcon

class CropExplanation : UncancelableDialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        builder()
            .setTitle("Welcome to AutoCrop \uD83C\uDF89")
            .setIcon(R.drawable.logo_wo_background)
            .setMessage(
                SpannableStringBuilder()
                    .append(
                        "You have just unlocked the capability to automatically crop multiple screenshots from your gallery & save them all in one go." +
                                "\n\n" +
                                "All you need to do now is press the "
                    )
                    .italic { append("Select Images ") }
                    .append("button and get croppin'!")
            )
            .setPositiveButton("Alright!") { _, _ -> }
            .create()

    interface OnDismissListener{
        fun onDismiss()
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)

        (requireActivity() as OnDismissListener).onDismiss()
    }
}

class ScreenshotListenerExplanation : UncancelableDialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        builder()
            .setTitle("Screenshot listening")
            .setIcon(requireContext().getColoredIcon(R.drawable.ic_screenshot_24, R.color.magenta_bright))
            .setMessage(
                SpannableStringBuilder().apply {
                    append(
                        "With screenshot listening enabled, you will get a notification whenever you take a croppable screenshot. " +
                                "Through that, you can save the crop and delete the original screenshot without having to open the app." +
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
            .setPositiveButton("Enable") { _, _ -> (requireActivity() as OnConfirmedListener).onConfirmed() }
            .create()

    interface OnConfirmedListener{
        fun onConfirmed()
    }
}