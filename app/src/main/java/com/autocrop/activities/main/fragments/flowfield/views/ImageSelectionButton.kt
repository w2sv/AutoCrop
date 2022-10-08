package com.autocrop.activities.main.fragments.flowfield.views

import android.content.Context
import android.content.Intent
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.findFragment
import com.autocrop.activities.main.fragments.flowfield.FlowFieldFragment
import com.autocrop.utils.android.IMAGE_MIME_TYPE

class ImageSelectionButton(context: Context, attributeSet: AttributeSet):
    AppCompatButton(context, attributeSet) {

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        setOnClickListener {
            with(findFragment<FlowFieldFragment>()){
                writeExternalStoragePermissionHandler.requestPermissionsOrRun {
                    imageSelectionIntentLauncher.launch(
                        Intent(Intent.ACTION_PICK).apply {
                            type = IMAGE_MIME_TYPE
                            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                        }
                    )
                }
            }
        }
    }
}