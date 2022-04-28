package com.autocrop.uicontroller.activity

import androidx.fragment.app.FragmentActivity

abstract class ExtendedActivity: FragmentActivity() {
    protected open fun displayEntrySnackbar() {}
}