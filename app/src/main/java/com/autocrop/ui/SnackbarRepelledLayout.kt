package com.autocrop.ui

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.material.snackbar.Snackbar.SnackbarLayout
import de.paul_woitaschek.slimber.i
import kotlin.math.min

class SnackbarRepelledLayout(context: Context, private val attributeSet: AttributeSet) :
    LinearLayout(
        context,
        attributeSet
    ),
    CoordinatorLayout.AttachedBehavior {

    init {
        i{"SnackbarRepelledLayout.init"}
    }

    class Behavior(context: Context?, attrs: AttributeSet?) : CoordinatorLayout.Behavior<View?>(context, attrs) {
        override fun layoutDependsOn(parent: CoordinatorLayout, child: View, dependency: View): Boolean =
            (dependency is SnackbarLayout)

        override fun onDependentViewChanged(parent: CoordinatorLayout, child: View, dependency: View): Boolean {
            child
                .apply {
                    clearAnimation()

                    val translation = min(0f, (dependency.translationY - dependency.height) * 0.75f)
                    translationY = translation
                    setPadding(0, -translation.toInt(), 0, 0)

//                    i{"onDependentViewChanged $translation"}
                }
            return true
        }

        override fun onDependentViewRemoved(parent: CoordinatorLayout, child: View, dependency: View) {
            child.setPadding(0, 0, 0, 0)
            super.onDependentViewRemoved(parent, child, dependency)
        }
    }

    override fun getBehavior(): CoordinatorLayout.Behavior<*> =
        Behavior(context, attributeSet)
            .also { i{"SnackbarRepelledLayout.getBehavior"} }
}