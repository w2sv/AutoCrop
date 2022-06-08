package com.autocrop.activities.main.fragments.flowfield.views

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageButton
import com.autocrop.activities.IntentExtraIdentifier
import com.autocrop.activities.main.MainActivity
import com.autocrop.retriever.activity.ActivityRetriever
import com.autocrop.retriever.activity.ContextBasedActivityRetriever
import com.autocrop.uielements.view.animate
import com.autocrop.uielements.view.show
import com.autocrop.utilsandroid.MimeTypes
import com.daimajia.androidanimations.library.Techniques
import com.w2sv.autocrop.R

class CropSharingButton(context: Context, attrs: AttributeSet):
    AppCompatImageButton(context, attrs),
    ActivityRetriever<MainActivity> by ContextBasedActivityRetriever(context) {

    /**
     * If CROP_SAVING_URIS available from previous ExaminationActivity cycle buildAndShow and
     * setOnClickListener
     */
    init {
        activity.intent.extras?.getParcelableArrayList<Uri>(IntentExtraIdentifier.CROP_SAVING_URIS)?.let {
            show()
            setOnClickListener(it)
        }
        animate(
            Techniques.Tada,
            delay = resources.getInteger(R.integer.duration_fade_in_flowfield_fragment_buttons).toLong() / 2
        )
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