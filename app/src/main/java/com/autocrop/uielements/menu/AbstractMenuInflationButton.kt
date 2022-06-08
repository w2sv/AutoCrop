package com.autocrop.uielements.menu

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageButton
import androidx.appcompat.widget.PopupMenu

abstract class AbstractMenuInflationButton(context: Context, attributeSet: AttributeSet)
    : AppCompatImageButton(context, attributeSet) {

    init {
        @Suppress("LeakingThis")
        setOnClickListener {
            popupMenu.show()
        }
    }

    private val popupMenu: PopupMenu by lazy {
        instantiatePopupMenu()
    }

    protected abstract fun instantiatePopupMenu(): PopupMenu
}