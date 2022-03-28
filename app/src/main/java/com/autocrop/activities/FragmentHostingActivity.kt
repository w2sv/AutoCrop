package com.autocrop.activities

import android.os.Bundle
import android.view.LayoutInflater
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import kotlin.properties.Delegates

abstract class FragmentHostingActivity<VB: ViewBinding>(inflateViewBinding: (LayoutInflater) -> VB)
    : ViewBindingHandlingActivity<VB>(inflateViewBinding) {

    abstract val rootFragment: Fragment
    protected var fragmentContainerViewId by Delegates.notNull<Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        onCreateCore()
        launchRootFragment(savedInstanceState)
    }

    protected abstract fun onCreateCore()

    private fun launchRootFragment(savedInstanceState: Bundle?){
        if (savedInstanceState == null)
            supportFragmentManager
                .beginTransaction()
                .add(fragmentContainerViewId, rootFragment)
                .commit()
    }

    fun replaceCurrentFragmentWith(fragment: Fragment, animationIds: Pair<Int, Int>? = null){
        supportFragmentManager
            .beginTransaction()
            .run {
                animationIds?.let {
                    setCustomAnimations(it.first, it.second)
                } ?: this
            }
            .replace(fragmentContainerViewId, fragment)
            .setReorderingAllowed(true)
            .commit()
    }
}