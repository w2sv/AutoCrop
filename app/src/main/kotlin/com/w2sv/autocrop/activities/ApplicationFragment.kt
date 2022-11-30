package com.w2sv.autocrop.activities

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.core.view.doOnPreDraw
import androidx.lifecycle.lifecycleScope
import androidx.viewbinding.ViewBinding
import com.w2sv.androidutils.extensions.getLong
import com.w2sv.androidutils.extensions.launchDelayed
import com.w2sv.autocrop.R
import com.w2sv.viewboundcontroller.ViewBoundFragment
import kotlinx.coroutines.CoroutineScope

abstract class ApplicationFragment<VB : ViewBinding>(bindingClass: Class<VB>) :
    ViewBoundFragment<VB>(bindingClass) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        postponeEnterTransition()

        super.onViewCreated(view, savedInstanceState)

        (view.parent as? ViewGroup)?.doOnPreDraw {
            startPostponedEnterTransition()
        }
    }

    @Suppress("UNCHECKED_CAST")
    protected fun <A : Activity> castActivity(): A =
        requireActivity() as A

    protected fun getFragmentHostingActivity(): FragmentHostingActivity =
        castActivity()

    protected fun launchAfterShortDelay(block: CoroutineScope.() -> Unit) {
        lifecycleScope.launchDelayed(resources.getLong(R.integer.delay_small), block = block)
    }
}