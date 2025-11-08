package com.w2sv.autocrop.ui.util.view

import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import androidx.activity.ComponentActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.viewbinding.ViewBinding
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

/**
 * Calls the given [viewBindingProvider] to create a ViewBinding of type [T] by binding the given
 * [LayoutInflater] and creates a [ViewBindingDelegate] that manages that [ViewBinding].
 *
 * @param A [ComponentActivity]
 * @param T [ViewBinding]
 * @param viewBindingProvider Creates the ViewBinding with the given view, usually a reference like
 *   MyActivityBinding::inflate
 * @param lifecycleOwnerProvider provide the LifecycleOwner. Default: [this]
 * @return [ViewBindingDelegate]
 */
fun <A : ComponentActivity, T : ViewBinding> A.viewBinding(
    viewBindingProvider: (LayoutInflater) -> T,
    lifecycleOwnerProvider: (A) -> LifecycleOwner = { this }
): ViewBindingDelegate<A, T> =
    ViewBindingDelegate(lifecycleOwnerProvider) { activity ->
        viewBindingProvider(activity.layoutInflater)
    }

/**
 * A delegate for [ViewBinding]s that automatically clears all view references when the [Lifecycle]
 * of the given [lifecycleOwnerProvider] gets in the destroyed state in order to prevent memory
 * leaks.
 *
 * Background: When the view hierarchy in a fragment gets destroyed but the fragment keeps a
 * reference to its [ViewBinding] property, the the garbage collector cannot clean up the views that
 * are referenced in the [ViewBinding]. This happens when navigating forward from fragment A to
 * fragment B while A is kept on the back stack. In this case, fragment A's view hierarchy gets
 * destroyed while A itself does not.
 */
class ViewBindingDelegate<in R : Any, T : ViewBinding>(
    private val lifecycleOwnerProvider: (R) -> LifecycleOwner,
    private val viewBindingProvider: (R) -> T
) : ReadOnlyProperty<R, T> {

    private val mainHandler = Handler(Looper.getMainLooper())
    private var viewBinding: T? = null
    private val lifecycleObserver = object : DefaultLifecycleObserver {
        override fun onDestroy(owner: LifecycleOwner) =
            clearAllReferences(owner)
    }

    override fun getValue(thisRef: R, property: KProperty<*>): T {
        viewBinding?.let { return it }

        val viewBinding = viewBindingProvider(thisRef)
        val lifecycle = lifecycleOwnerProvider(thisRef).lifecycle

        if (lifecycle.currentState == Lifecycle.State.DESTROYED) {
            mainHandler.post { this.viewBinding = null }
        } else {
            lifecycle.addObserver(lifecycleObserver)
            this.viewBinding = viewBinding
        }
        return viewBinding
    }

    private fun clearAllReferences(lifecycleOwner: LifecycleOwner) {
        viewBinding
            ?: return

        lifecycleOwner.lifecycle.removeObserver(lifecycleObserver)
        mainHandler.post { viewBinding = null }
    }
}
