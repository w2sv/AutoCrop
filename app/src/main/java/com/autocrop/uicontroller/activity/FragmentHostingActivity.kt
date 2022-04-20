package com.autocrop.uicontroller.activity

import android.os.Bundle
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.viewbinding.ViewBinding
import com.autocrop.utils.reflectField
import com.autocrop.utils.reflectMethod
import com.w2sv.autocrop.R

abstract class FragmentHostingActivity<VB: ViewBinding>
    : ViewBindingHandlingActivity<VB>() {

    /**
     * Fragment being launched before exiting [onCreateCore]
     */
    abstract val rootFragment: Fragment

    /**
     * Retrieved bmo reflection from view binding;
     *
     * Note: Imposes the need for activity layout to carry the id "layout"
     */
    private val layoutId: Int by lazy {
        binding.reflectField<ViewGroup>("layout").reflectMethod("id")
    }

    /**
     * Run [onCreateCore] and [launchRootFragment] if applicable
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

    fun swapFragments(hideFragment: Fragment, showFragment: Fragment){
        supportFragmentManager
            .beginTransaction()
            .setCustomAnimations(
                R.animator.card_flip_left_in,
                R.animator.card_flip_left_out,
                R.animator.card_flip_right_in,
                R.animator.card_flip_right_out
            )
            .hide(hideFragment)
            .addIfNecessaryAndShow(showFragment)
            .setReorderingAllowed(true)
            .commit()
    }

    private fun FragmentTransaction.addIfNecessaryAndShow(fragment: Fragment): FragmentTransaction =
        if (fragment !in supportFragmentManager.fragments)
            add(layoutId, fragment)
        else
            show(fragment)
}