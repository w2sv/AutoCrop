package com.autocrop.activities.main.fragments.flowfield.views

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageButton
import com.autocrop.activities.main.fragments.flowfield.FlowFieldPopupMenu

class MenuInflationButton(context: Context, attributeSet: AttributeSet)
    : AppCompatImageButton(context, attributeSet) {

    init {
        setOnClickListener {
            popupMenu.show()
        }
    }

    private val popupMenu by lazy {
        FlowFieldPopupMenu(
            context,
            this
        )
    }
}