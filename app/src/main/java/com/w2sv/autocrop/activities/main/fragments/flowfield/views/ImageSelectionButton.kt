package com.w2sv.autocrop.activities.main.fragments.flowfield.views

import android.content.Context
import android.content.Intent
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.findFragment
import com.w2sv.autocrop.activities.main.fragments.flowfield.FlowFieldFragment
import com.w2sv.autocrop.utils.android.IMAGE_MIME_TYPE

class ImageSelectionButton(context: Context, attributeSet: AttributeSet) :
    AppCompatButton(context, attributeSet) {

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        setOnClickListener {
            with(findFragment<FlowFieldFragment>()) {
                writeExternalStoragePermissionHandler.requestPermission(
                    {
                        imageSelectionIntentLauncher.launch(
                            Intent(Intent.ACTION_PICK).apply {
                                type = IMAGE_MIME_TYPE
                                putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                            }
                        )
                    }
                )
            }
        }
    }
}