package com.autocrop.utils.android

import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction

fun FragmentTransaction.addIfNecessaryAndShow(fragment: Fragment, @IdRes layoutId: Int, fragmentManager: FragmentManager): FragmentTransaction =
    if (fragment !in fragmentManager.fragments)
        add(layoutId, fragment)
    else
        show(fragment)