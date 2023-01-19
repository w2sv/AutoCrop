package com.w2sv.autocrop.activities

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.doOnPreDraw
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.lifecycleScope
import androidx.viewbinding.ViewBinding
import com.w2sv.androidutils.extensions.getLong
import com.w2sv.androidutils.extensions.launchDelayed
import com.w2sv.autocrop.R
import com.w2sv.viewboundcontroller.ViewBoundFragment
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import javax.inject.Inject

typealias AppFragmentReceiver = AppFragment<*>.() -> Unit

abstract class AppFragment<VB : ViewBinding>(bindingClass: Class<VB>) :
    ViewBoundFragment<VB>(bindingClass) {

    companion object {
        const val EXTRA_ON_FRAGMENT_FINISHED_LISTENER = "com.w2sv.autocrop.extra.ON_FRAGMENT_FINISHED_LISTENER"

        fun <F: AppFragment<*>> getInstance(clazz: Class<F>, onFinishedListener: AppFragmentReceiver): F =
            clazz.newInstance()
                .apply {
                    arguments = bundleOf(
                        EXTRA_ON_FRAGMENT_FINISHED_LISTENER to onFinishedListener
                    )
                }
    }

    @HiltViewModel
    class ViewModel @Inject constructor(savedStateHandle: SavedStateHandle) : androidx.lifecycle.ViewModel() {
        val onFinishedListener: AppFragmentReceiver =
            savedStateHandle[EXTRA_ON_FRAGMENT_FINISHED_LISTENER]!!
    }

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

    @Suppress("UNCHECKED_CAST")
    fun <A : Activity> castActivity(): A =
        requireActivity() as A

    fun fragmentHostingActivity(): FragmentedActivity =
        castActivity()

    protected fun launchAfterShortDelay(block: CoroutineScope.() -> Unit) {
        lifecycleScope.launchDelayed(resources.getLong(R.integer.delay_small), block = block)
    }
}