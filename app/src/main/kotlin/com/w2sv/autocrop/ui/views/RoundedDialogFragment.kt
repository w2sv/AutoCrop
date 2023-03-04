package com.w2sv.autocrop.ui.views

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.w2sv.autocrop.R

abstract class RoundedDialogFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        MaterialAlertDialogBuilder(requireContext(), R.style.MaterialAlertDialogRounded)
            .build()
            .create()
            .apply {
                onCreatedListener()
            }

    abstract fun AlertDialog.Builder.build(): AlertDialog.Builder

    open fun AlertDialog.onCreatedListener() {}
}