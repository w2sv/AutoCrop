package com.autocrop.activities.main

import android.app.Dialog
import android.content.DialogInterface
import android.os.Build
import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import com.autocrop.utils.android.extensions.show
import com.autocrop.views.UncancelableDialogFragment
import com.w2sv.autocrop.R

class CropExplanation: UncancelableDialogFragment(){
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        builder()
            .setTitle("Welcome to AutoCrop \uD83C\uDF89")
            .setIcon(R.drawable.logo_wo_background)
            .setMessage(
                "You have just unlocked the superpower of automatically cropping multiple screenshots from your gallery & saving them all in one go.\n\n" +
                "Just press the 'Select Images' button and get croppin'!"
            )
            .setPositiveButton("Alright!"){_, _ -> }
            .create()

    override fun onDismiss(dialog: DialogInterface) {
        ScreenshotListenerExplanation().show(parentFragmentManager)
        super.onDismiss(dialog)
    }
}

class ScreenshotListenerExplanation: UncancelableDialogFragment(){
    companion object{
        const val REQUEST_KEY = "ScreenshotListenerExplanation.REQUEST_KEY"

        fun dialogConfirmed(bundle: Bundle): Boolean =
            bundle.getBoolean(EXTRA_INQUIRE_PERMISSIONS)

        private const val EXTRA_INQUIRE_PERMISSIONS = "com.autocrop.INQUIRE_PERMISSIONS"
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        builder()
            .setTitle("Screenshot listener")
            .setIcon(R.drawable.ic_screenshot_24)
            .setMessage(
                buildString {
                    append(
                        "With screenshot listening, you will get a notification whenever you take a croppable screenshot. " +
                        "Through that, you can save the crop and delete the original screenshot, without having to open the app." +
                        "\n\n" +
                        "For this to work, the media file reading "
                    )
                    (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU).let {notificationPermissionRequired ->
                        if (notificationPermissionRequired)
                            append("& notification posting ")
                        append("permission")
                        if (notificationPermissionRequired)
                            append("s are ")
                        else
                            append(" is ")
                    }
                    append("required.\n\nYou can en-/disable the screenshot listener anytime through the drawer menu.")
                }
            )
            .setNegativeButton("Don't enable"){_, _ -> setFragmentResult(false) }
            .setPositiveButton("Enable"){_, _ -> setFragmentResult(true) }
            .create()

    private fun setFragmentResult(confirmed: Boolean){
        setFragmentResult(
            REQUEST_KEY,
            bundleOf(
                EXTRA_INQUIRE_PERMISSIONS to confirmed
            )
        )
    }
}