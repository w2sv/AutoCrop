package com.autocrop.uicontroller.activity

import android.os.Bundle
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import com.autocrop.utils.reflectField
import com.autocrop.utils.reflectMethod

abstract class FragmentHostingActivity<VB: ViewBinding>
    : ViewBindingHandlingActivity<VB>() {

    /**
     * Fragment being launched before exiting [onCreateCore]
     */
    abstract val rootFragment: Fragment

    /**
     * Retrieved bmo reflection from view binding;
     *
     * Imposes the need for activity layout to carry the id "layout"
     */
    private val layoutId: Int by lazy {
        binding.reflectField<ViewGroup>("layout").reflectMethod("id")
    }

    /**
     * Run [onCreateCore] and thereupon [launchRootFragment] if applicable
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        onCreateCore()
        if (savedInstanceState == null)
            launchRootFragment()
    }

    protected open fun onCreateCore() = Unit

    private fun launchRootFragment(){
        supportFragmentManager
            .beginTransaction()
            .add(layoutId, rootFragment)
            .commit()
    }

    fun replaceCurrentFragmentWith(fragment: Fragment, animationIds: Pair<Int, Int>? = null){
        supportFragmentManager
            .beginTransaction()
            .apply {
                animationIds?.let {
                    setCustomAnimations(it.first, it.second)
                }
            }
            .replace(layoutId, fragment)
            .setReorderingAllowed(true)
            .commit()
    }

    fun swapFragments(hideFragment: Fragment, showFragment: Fragment, animationIds: Pair<Int, Int>? = null){
        supportFragmentManager
            .beginTransaction()
            .setReorderingAllowed(true)
            .hide(hideFragment)
            .apply {
                animationIds?.let {
                    setCustomAnimations(it.first, it.second)
                }
            }
            .apply {
                if (showFragment !in supportFragmentManager.fragments)
                    add(layoutId, showFragment)
                else
                    show(showFragment)
            }
            .commit()
    }
}