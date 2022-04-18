package com.autocrop.uielements

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.appcompat.widget.AppCompatTextView
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo
import com.w2sv.autocrop.R

fun View.show() { visibility = View.VISIBLE }
fun View.remove() { visibility = View.GONE}

fun crossFade(fadeInView: View, fadeOutView: View, animationDuration: Long){
    fadeInView.fadeIn(animationDuration)
    fadeOutView.fadeOut(animationDuration)
}

fun View.fadeIn(duration: Long){
    alpha = 0f
    show()

    animate()
        .alpha(1f)
        .duration = duration
}

fun View.fadeOut(duration: Long){
    animate()
        .alpha(0f)
        .setListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                remove()
            }
        })
        .duration = duration
}

fun View.setAnimation(technique: Techniques): YoYo.YoYoString =
    YoYo.with(technique)
        .duration(context.resources.getInteger(R.integer.yoyo_animation_duration).toLong())
        .playOn(this)

abstract class ExtendedTextView(context: Context, attr: AttributeSet, private val stringId: Int)
    : AppCompatTextView(context, attr) {

    protected fun getString(): String = context.resources.getString(stringId)
}

interface ViewModelRetriever<VM: ViewModel>{
    val viewModel: VM
}

abstract class AbstractContextBasedViewModelRetriever<VM: ViewModel, VMST: ViewModelStoreOwner>(context: Context, viewModelClass: Class<VM>)
    : ViewModelRetriever<VM> {

    @Suppress("UNCHECKED_CAST")
    override val viewModel: VM by lazy {
        ViewModelProvider(context as VMST)[viewModelClass]
    }
}