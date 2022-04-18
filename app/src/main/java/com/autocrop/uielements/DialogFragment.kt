package com.autocrop.uielements

import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager

abstract class ExtendedDialogFragment
    : DialogFragment(){
        fun show(fragmentManager: FragmentManager) = show(fragmentManager, this::class.java.name)
    }