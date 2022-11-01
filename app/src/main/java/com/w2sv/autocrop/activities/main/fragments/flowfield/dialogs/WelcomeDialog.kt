package com.w2sv.autocrop.activities.main.fragments.flowfield.dialogs

import android.app.Dialog
import android.os.Bundle
import android.text.SpannableStringBuilder
import androidx.core.text.italic
import com.w2sv.autocrop.R
import com.w2sv.autocrop.ui.views.UncancelableDialogFragment

class WelcomeDialog : UncancelableDialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        builder()
            .setIcon(R.drawable.logo_nobackground)
            .setTitle("Welcome to AutoCrop \uD83C\uDF89")
            .setMessage(
                SpannableStringBuilder()
                    .append("All you need to do from here is press the ")
                    .italic { append("Select Images ") }
                    .append("button and save your first crops!")
            )
            .setPositiveButton("Alright!") { _, _ -> (parentFragment as OnProceedListener).onProceed() }
            .create()

    interface OnProceedListener {
        fun onProceed()
    }
}