package com.w2sv.autocrop.activities.main.fragments.about.views

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.appcompat.widget.AppCompatTextView
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.findViewTreeLifecycleOwner
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo.AnimationComposer
import com.daimajia.androidanimations.library.YoYo.YoYoString
import com.w2sv.androidutils.extensions.goToWebpage
import com.w2sv.autocrop.R
import com.w2sv.autocrop.ui.animationComposer
import java.util.Calendar

class CopyrightTextView(context: Context, attr: AttributeSet) :
    AppCompatTextView(context, attr) {

    private lateinit var animation: LifecycleAwareAnimationWrapper

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        text = resources.getString(R.string.copyright, Calendar.getInstance().get(Calendar.YEAR))

        animation = LifecycleAwareAnimationWrapper(
            animationComposer(Techniques.ZoomOutRight)
                .onEnd {
                    context.goToWebpage("https://github.com/w2sv/AutoCrop/blob/master/LICENSE")
                },
            findViewTreeLifecycleOwner()!!
        )

        setOnClickListener {
            animation.playOn(this)
        }
    }
}

private class LifecycleAwareAnimationWrapper(private val animationComposer: AnimationComposer, lifecycleOwner: LifecycleOwner) : DefaultLifecycleObserver {

    private var animation: YoYoString? = null

    init {
        lifecycleOwner.lifecycle.addObserver(this)
    }

    override fun onResume(owner: LifecycleOwner) {
        super.onResume(owner)

        animation?.stop(true)
    }

    fun playOn(target: View) {
        animation = animationComposer.playOn(target)
    }
}