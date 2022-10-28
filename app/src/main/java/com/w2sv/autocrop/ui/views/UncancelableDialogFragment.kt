package com.w2sv.autocrop.ui.views

import android.app.AlertDialog
import androidx.fragment.app.DialogFragment

abstract class UncancelableDialogFragment : DialogFragment() {
    init {
        isCancelable = false
    }

    protected fun builder(): AlertDialog.Builder =
        AlertDialog.Builder(requireContext())
            .setCancelable(false)
}