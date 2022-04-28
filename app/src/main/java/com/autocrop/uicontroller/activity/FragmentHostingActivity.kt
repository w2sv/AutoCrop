package com.autocrop.uicontroller.activity

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.viewbinding.ViewBinding
import com.w2sv.autocrop.R

abstract class FragmentHostingActivity<VB: ViewBinding>
    : ViewBindingHandlingActivity<VB>() {

    /**
     * Fragment being launched in [onCreate]
     */
    abstract val rootFragment: Fragment

    private val layoutId: Int by lazy { binding.root.id }

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
            .setReorderingAllowed(true)
            .add(layoutId, rootFragment)
            .commit()
    }

    fun currentFragment(): Fragment? =
        supportFragmentManager.findFragmentById(layoutId)

    protected companion object{
        val leftFlipAnimationIds = R.animator.card_flip_left_in to R.animator.card_flip_left_out
        val rightFlipAnimationIds = R.animator.card_flip_right_in to R.animator.card_flip_right_out
    }

    fun replaceCurrentFragmentWith(fragment: Fragment, animationIds: Pair<Int, Int>? = null){
        supportFragmentManager
            .beginTransaction()
            .setReorderingAllowed(true)
            .apply {
                animationIds?.let {
                    setCustomAnimations(it.first, it.second)
                }
            }
            .replace(layoutId, fragment)
            .commit()
    }

    fun swapFragments(hideFragment: Fragment, showFragment: Fragment){
        supportFragmentManager
            .beginTransaction()
            .setReorderingAllowed(true)
            .setCustomAnimations(
                leftFlipAnimationIds.first,
                leftFlipAnimationIds.second,
                rightFlipAnimationIds.first,
                rightFlipAnimationIds.second
            )
            .hide(hideFragment)
            .addIfNecessaryAndShow(showFragment)
            .commit()
    }

    private fun FragmentTransaction.addIfNecessaryAndShow(fragment: Fragment): FragmentTransaction =
        if (fragment !in supportFragmentManager.fragments)
            add(layoutId, fragment)
        else
            show(fragment)
}