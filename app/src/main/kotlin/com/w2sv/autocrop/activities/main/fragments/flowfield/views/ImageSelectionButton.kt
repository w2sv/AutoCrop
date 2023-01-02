package com.w2sv.autocrop.activities.main.fragments.flowfield.views

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.findFragment
import com.w2sv.autocrop.activities.main.fragments.flowfield.FlowFieldFragment

class ImageSelectionButton(context: Context, attributeSet: AttributeSet) :
    AppCompatButton(context, attributeSet) {

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        setOnClickListener {
            with(findFragment<FlowFieldFragment>()) {
                writeExternalStoragePermissionHandler.requestPermission(
                    onGranted = selectImagesContractHandler::selectImages
                )
            }
        }
    }
}