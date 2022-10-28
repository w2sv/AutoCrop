package com.w2sv.autocrop.utils.android.extensions

import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager

fun DialogFragment.show(fragmentManager: FragmentManager) =
    show(fragmentManager, this::class.java.name)