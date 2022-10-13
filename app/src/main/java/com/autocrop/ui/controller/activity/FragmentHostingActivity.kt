package com.autocrop.ui.controller.activity

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.w2sv.autocrop.R

abstract class FragmentHostingActivity<RF : Fragment>(private val rootFragmentClass: Class<RF>) : ViewBoundActivity() {

    companion object {
        private const val ROOT_FRAGMENT_TAG = "ROOT_FRAGMENT"
    }

    private val layoutId: Int get() = binding.root.id

    protected open fun onSavedInstanceStateNull() {
        launchRootFragment()
    }

    private fun launchRootFragment() {
        supportFragmentManager
            .beginTransaction()
            .setReorderingAllowed(true)
            .add(
                layoutId,
                rootFragmentClass.newInstance(),
                ROOT_FRAGMENT_TAG
            )
            .commit()
    }

    @Suppress("UNCHECKED_CAST")
    fun getRootFragment(): RF? =
        supportFragmentManager.findFragmentByTag(ROOT_FRAGMENT_TAG) as RF?

    @Suppress("UNCHECKED_CAST")
    fun <F : Fragment> getCurrentFragment(): F? =
        supportFragmentManager.findFragmentById(layoutId) as F?

    fun fragmentReplacementTransaction(fragment: Fragment, flipRight: Boolean? = null): FragmentTransaction =
        supportFragmentManager
            .beginTransaction()
            .setReorderingAllowed(true)
            .apply {
                flipRight?.let {
                    with(
                        if (it)
                            R.animator.card_flip_left_in to R.animator.card_flip_left_out
                        else
                            R.animator.card_flip_right_in to R.animator.card_flip_right_out
                    ) {
                        setCustomAnimations(first, second, first, second)
                    }
                }
            }
            .replace(layoutId, fragment)
}