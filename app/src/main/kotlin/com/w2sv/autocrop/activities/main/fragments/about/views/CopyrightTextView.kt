package com.w2sv.autocrop.activities.main.fragments.about.views

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import androidx.lifecycle.findViewTreeLifecycleOwner
import com.daimajia.androidanimations.library.Techniques
import com.w2sv.androidutils.extensions.goToWebpage
import com.w2sv.autocrop.R
import com.w2sv.autocrop.ui.LifecycleAwareAnimation
import com.w2sv.autocrop.ui.animationComposer
import java.util.Calendar

class CopyrightTextView(context: Context, attr: AttributeSet) :
    AppCompatTextView(context, attr) {

    private lateinit var animation: LifecycleAwareAnimation

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        text = resources.getString(R.string.copyright, Calendar.getInstance().get(Calendar.YEAR))

        animation = LifecycleAwareAnimation(
            animationComposer(Techniques.ZoomOutRight)
                .onEnd {
                    context.goToWebpage("https://github.com/w2sv/AutoCrop/blob/master/LICENSE")
                },
            findViewTreeLifecycleOwner()!!
        )

        setOnClickListener {
            animation.play()
        }
    }
}