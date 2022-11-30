package com.w2sv.autocrop.activities.main.fragments.about.views

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.findViewTreeLifecycleOwner
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo
import com.w2sv.androidutils.extensions.goToWebpage
import com.w2sv.autocrop.R
import com.w2sv.autocrop.ui.animationComposer
import java.util.Calendar

class CopyrightTextView(context: Context, attr: AttributeSet) :
    AppCompatTextView(context, attr),
    DefaultLifecycleObserver {

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        if (!isInEditMode)
            findViewTreeLifecycleOwner()!!.lifecycle.addObserver(this)

        text = resources.getString(R.string.copyright, Calendar.getInstance().get(Calendar.YEAR))

        setOnClickListener {
            animation = it
                .animationComposer(Techniques.ZoomOutRight)
                .onEnd {
                    context.goToWebpage("https://github.com/w2sv/AutoCrop/blob/master/LICENSE")
                }
                .playOn(it)
        }
    }

    private var animation: YoYo.YoYoString? = null

    override fun onResume(owner: LifecycleOwner) {
        super.onResume(owner)

        animation?.stop(true)
    }
}