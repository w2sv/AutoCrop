package com.autocrop.activities.main.fragments.flowfield.views

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageButton
import com.autocrop.activities.IntentExtraIdentifier
import com.autocrop.activities.main.MainActivity
import com.autocrop.retriever.activity.ActivityRetriever
import com.autocrop.retriever.activity.ContextBasedActivityRetriever
import com.autocrop.ui.elements.view.animate
import com.autocrop.ui.elements.view.show
import com.autocrop.utils.android.IMAGE_MIME_TYPE
import com.daimajia.androidanimations.library.Techniques
import com.w2sv.autocrop.R

class CropSharingButton(context: Context, attrs: AttributeSet):
    AppCompatImageButton(context, attrs),
    ActivityRetriever<MainActivity> by ContextBasedActivityRetriever(context) {

    /**
     * If CROP_SAVING_URIS available from previous ExaminationActivity cycle show and
     * setOnClickListener
     */
    init {
        (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            activity.intent.extras?.getParcelableArrayList(IntentExtraIdentifier.CROP_SAVING_URIS, Uri::class.java)
        else
            @Suppress("DEPRECATION")
            activity.intent.extras?.getParcelableArrayList(IntentExtraIdentifier.CROP_SAVING_URIS))
                ?.let {
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
                        type = IMAGE_MIME_TYPE
                    },
                    null
                )
            )
        }
}