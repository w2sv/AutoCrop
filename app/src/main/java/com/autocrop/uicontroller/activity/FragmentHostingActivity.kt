package com.autocrop.uicontroller.activity

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.w2sv.autocrop.R

abstract class FragmentHostingActivity<RF: Fragment>(
    private val rootFragmentClass: Class<RF>)
        : ViewBoundActivity() {

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
            .add(layoutId, rootFragmentClass.newInstance(), ROOT_FRAGMENT_TAG)
            .commit()
    }

    fun rootFragment(): Fragment? =
        supportFragmentManager.findFragmentByTag(ROOT_FRAGMENT_TAG)

    fun currentFragment(): Fragment? =
        supportFragmentManager.findFragmentById(layoutId)

    private companion object{
        const val ROOT_FRAGMENT_TAG = "ROOT_FRAGMENT"

        val leftFlipAnimationIds = R.animator.card_flip_left_in to R.animator.card_flip_left_out
        val rightFlipAnimationIds = R.animator.card_flip_right_in to R.animator.card_flip_right_out
    }

    fun replaceCurrentFragmentWith(fragment: Fragment, flipRight: Boolean? = null, additionalCalls: ((FragmentTransaction) -> FragmentTransaction)? = null){
        supportFragmentManager
            .beginTransaction()
            .setReorderingAllowed(true)
            .apply {
                flipRight?.let {
                    with(if (it) rightFlipAnimationIds else leftFlipAnimationIds){
                        setCustomAnimations(first, second)
                    }
                }
                additionalCalls?.let {
                    it(this)
                }
            }
            .replace(layoutId, fragment)
            .commit()
    }
}