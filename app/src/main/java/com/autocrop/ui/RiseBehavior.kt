package com.autocrop.ui

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import androidx.annotation.Keep
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.material.snackbar.Snackbar.SnackbarLayout
import kotlin.math.min

@Keep
class RiseBehavior(context: Context?, attrs: AttributeSet?) : CoordinatorLayout.Behavior<View?>(context, attrs) {
    override fun layoutDependsOn(parent: CoordinatorLayout, child: View, dependency: View): Boolean =
        (dependency is SnackbarLayout)

    override fun onDependentViewChanged(parent: CoordinatorLayout, child: View, dependency: View): Boolean {
        child.animate().translationY(min(0f, dependency.translationY - dependency.height))
        return true
    }
}

class RisingLinearLayout(context: Context, private val attributeSet: AttributeSet) :
    LinearLayout(
        context,
        attributeSet
    ),
    CoordinatorLayout.AttachedBehavior {

    override fun getBehavior(): CoordinatorLayout.Behavior<*> =
        RiseBehavior(context, attributeSet)
}