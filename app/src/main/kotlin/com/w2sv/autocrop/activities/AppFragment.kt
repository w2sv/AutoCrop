package com.w2sv.autocrop.activities

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.core.view.doOnPreDraw
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.lifecycleScope
import androidx.viewbinding.ViewBinding
import com.w2sv.androidutils.extensions.getLong
import com.w2sv.androidutils.extensions.launchDelayed
import com.w2sv.autocrop.R
import com.w2sv.viewboundcontroller.ViewBoundFragment
import kotlinx.coroutines.CoroutineScope

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

    /**
     * Activity retrieval
     */

    @Suppress("UNCHECKED_CAST")
    fun <A : Activity> requireCastActivity(): A =
        requireActivity() as A

    fun requireViewBoundFragmentActivity(): ViewBoundFragmentActivity =
        requireCastActivity()
}