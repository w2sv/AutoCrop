package com.w2sv.autocrop.activities.main.fragments.about.views

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import com.daimajia.androidanimations.library.Techniques
import com.w2sv.autocrop.controller.activity.retriever.ActivityRetriever
import com.w2sv.autocrop.controller.activity.retriever.ContextBasedActivityRetriever
import com.w2sv.autocrop.ui.views.AnimationHandler
import com.w2sv.autocrop.utils.android.extensions.animationComposer
import com.w2sv.autocrop.utils.android.extensions.goToWebpage
import com.w2sv.autocrop.utils.android.extensions.textString
import java.util.Calendar

class CopyrightTextView(context: Context, attr: AttributeSet) :
    AppCompatTextView(context, attr),
    AnimationHandler by AnimationHandler.Implementation(),
    ActivityRetriever by ContextBasedActivityRetriever(context) {

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        text = textString.format(Calendar.getInstance().get(Calendar.YEAR))

        setOnClickListener {
            animation = it
                .animationComposer(Techniques.ZoomOutRight)
                .onEnd {
                    activity.goToWebpage("https://github.com/w2sv/AutoCrop/blob/master/LICENSE")
                }
                .playOn(it)
        }
    }
}