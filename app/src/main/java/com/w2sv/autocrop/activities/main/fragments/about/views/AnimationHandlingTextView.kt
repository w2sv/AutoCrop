package com.w2sv.autocrop.activities.main.fragments.about.views

import android.content.Context
import android.util.AttributeSet
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo
import com.w2sv.autocrop.controller.activity.retriever.ActivityRetriever
import com.w2sv.autocrop.controller.activity.retriever.ContextBasedActivityRetriever
import com.w2sv.autocrop.ui.views.ExtendedAppCompatTextView
import com.w2sv.autocrop.utils.android.extensions.animationComposer
import com.w2sv.autocrop.utils.android.extensions.goToWebpage

abstract class AnimationHandlingTextView(
    context: Context,
    attr: AttributeSet,
    techniques: Techniques,
    url: String
) :
    ExtendedAppCompatTextView(context, attr),
    DefaultLifecycleObserver,
    ActivityRetriever by ContextBasedActivityRetriever(context) {

    private var animation: YoYo.YoYoString? = null

    init {
        @Suppress("LeakingThis")
        setOnClickListener {
            animation = it
                .animationComposer(techniques)
                .onEnd {
                    activity.goToWebpage(url)
                }
                .playOn(it)
        }
    }

    override fun onResume(owner: LifecycleOwner) {
        super.onResume(owner)

        animation?.stop(true)
    }
}