package com.w2sv.autocrop.activities

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.lifecycleScope
import androidx.viewbinding.ViewBinding
import com.google.android.material.snackbar.Snackbar
import com.w2sv.androidutils.extensions.getLong
import com.w2sv.androidutils.extensions.launchDelayed
import com.w2sv.autocrop.R
import com.w2sv.common.extensions.snackyBuilder
import com.w2sv.viewboundcontroller.ViewBoundFragment
import de.mateware.snacky.Snacky
import kotlinx.coroutines.CoroutineScope

fun <F : Fragment> getFragment(clazz: Class<F>, vararg bundlePairs: Pair<String, Any?>): F =
    clazz.newInstance()
        .apply {
            arguments = bundleOf(*bundlePairs)
        }

abstract class AppFragment<VB : ViewBinding>(bindingClass: Class<VB>) :
    ViewBoundFragment<VB>(bindingClass) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        postponeEnterTransition()

        super.onViewCreated(view, savedInstanceState)

        (view.parent as? ViewGroup)?.doOnPreDraw {
            startPostponedEnterTransition()
        }
    }

    protected fun registerLifecycleObservers(observers: Iterable<LifecycleObserver>) {
        observers.forEach(lifecycle::addObserver)
    }

    protected fun launchAfterShortDelay(block: CoroutineScope.() -> Unit) {
        lifecycleScope.launchDelayed(resources.getLong(R.integer.delay_small), block = block)
    }

    protected fun getSnackyBuilder(text: CharSequence, duration: Int = Snackbar.LENGTH_LONG): Snacky.Builder =
        requireActivity()
            .snackyBuilder(text, duration)
            .apply {
                snackbarAnchorView?.let {
                    setView(it)
                }
            }

    protected open val snackbarAnchorView: View? get() = null

    /**
     * Activity retrieval
     */

    @Suppress("UNCHECKED_CAST")
    fun <A : Activity> castActivity(): A =
        requireActivity() as A

    fun requireViewBoundFragmentActivity(): ViewBoundFragmentActivity =
        castActivity()

    fun requireAppActivity(): AppActivity =
        castActivity()
}