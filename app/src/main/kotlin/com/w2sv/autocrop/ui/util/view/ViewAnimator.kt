package com.w2sv.autocrop.ui.util.view

import android.view.View
import android.widget.ViewAnimator

/**
 * Convenience function to display the passed [child].
 *
 * @throws [IllegalArgumentException] if the passed [child] isn't actually a [ViewAnimator] child.
 */
fun ViewAnimator.displayChild(child: View) {
    displayedChild = indexOfChild(child)
        .also { require(it != -1) { "View $child is no child of $this" } }
}
