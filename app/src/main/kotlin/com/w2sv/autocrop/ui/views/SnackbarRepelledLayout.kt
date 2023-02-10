package com.w2sv.autocrop.ui.views

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import com.google.android.material.snackbar.Snackbar
import com.w2sv.androidutils.ui.RepelledLayout

@SuppressLint("RestrictedApi")
class SnackbarRepelledLayout(context: Context, attributeSet: AttributeSet) : RepelledLayout<Snackbar.SnackbarLayout>(
    context,
    attributeSet,
    Snackbar.SnackbarLayout::class
)