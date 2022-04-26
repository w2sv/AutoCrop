package com.autocrop.activities.main.fragments.flowfield

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageButton
import com.autocrop.activities.IntentExtraIdentifier
import com.autocrop.uielements.view.show
import com.autocrop.utils.android.MimeTypes

class CropSharingButton(context: Context, attrs: AttributeSet)
    : AppCompatImageButton(context, attrs){

    /**
     * If CROP_SAVING_URIS available from previous ExaminationActivity cycle show and
     * setOnClickListener
     */
    init {
        (context as Activity).intent.extras?.getParcelableArrayList<Uri>(IntentExtraIdentifier.CROP_SAVING_URIS)?.let {
            show()
            setOnClickListener(it)
        }
    }

    private fun setOnClickListener(cropWriteUris: ArrayList<Uri>) =
        setOnClickListener {
            context.startActivity(
                Intent.createChooser(
                    Intent().apply {
                        action = Intent.ACTION_SEND_MULTIPLE
                        putParcelableArrayListExtra(Intent.EXTRA_STREAM, cropWriteUris)
                        type = MimeTypes.IMAGE
                    },
                    null
                )
            )
        }
}