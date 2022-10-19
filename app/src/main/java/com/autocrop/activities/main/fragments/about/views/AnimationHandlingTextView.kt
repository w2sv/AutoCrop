package com.autocrop.activities.main.fragments.about.views

import android.content.Context
import android.util.AttributeSet
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.autocrop.activities.main.MainActivity
import com.autocrop.uicontroller.activity.retriever.ActivityRetriever
import com.autocrop.uicontroller.activity.retriever.ContextBasedActivityRetriever
import com.autocrop.utils.android.extensions.animate
import com.autocrop.utils.android.extensions.goToWebpage
import com.autocrop.views.ExtendedAppCompatTextView
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo

abstract class AnimationHandlingTextView(
    context: Context,
    attr: AttributeSet,
    techniques: Techniques,
    url: String
):
    ExtendedAppCompatTextView(context, attr),
    DefaultLifecycleObserver,
    ActivityRetriever<MainActivity> by ContextBasedActivityRetriever(context) {

    private var animation: YoYo.YoYoString? = null

    init {
        @Suppress("LeakingThis")
        setOnClickListener{
            animation = it.animate(techniques){
                activity.goToWebpage(url)
            }
        }
    }

    override fun onResume(owner: LifecycleOwner) {
        super.onResume(owner)

        animation?.stop(true)
    }
}