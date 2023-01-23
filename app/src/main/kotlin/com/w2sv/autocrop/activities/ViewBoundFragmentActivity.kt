package com.w2sv.autocrop.activities

import android.annotation.SuppressLint
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.blogspot.atifsoftwares.animatoolib.R
import com.w2sv.autocrop.databinding.ActivityBlankBinding
import com.w2sv.viewboundcontroller.ViewBoundActivity

abstract class ViewBoundFragmentActivity : ViewBoundActivity<ActivityBlankBinding>(ActivityBlankBinding::class.java) {

    private val layoutId: Int get() = binding.root.id

    /**
     * Fragment transactions
     */

    @SuppressLint("CommitTransaction")
    protected fun launchRootFragment() {
        supportFragmentManager
            .beginTransaction()
            .setReorderingAllowed(true)
            .add(
                layoutId,
                getRootFragment(),
                "${this::class.java.name}_ROOT_FRAGMENT"
            )
            .commit()
    }

    @SuppressLint("CommitTransaction")
    fun fragmentReplacementTransaction(fragment: Fragment, animated: Boolean = false): FragmentTransaction =
        supportFragmentManager
            .beginTransaction()
            .apply {
                if (animated)
                    setCustomAnimations(
                        R.anim.animate_in_out_enter,
                        R.anim.animate_in_out_exit,
                        R.anim.animate_in_out_enter,
                        R.anim.animate_in_out_exit
                    )
            }
            .setReorderingAllowed(true)
            .replace(layoutId, fragment)

    /**
     * Fragment retrieval
     */

    abstract fun getRootFragment(): Fragment

    fun getCurrentFragment(): Fragment? =
        supportFragmentManager.findFragmentById(layoutId)

    @Suppress("UNCHECKED_CAST")
    fun <F : Fragment> getCastCurrentFragment(): F? =
        getCurrentFragment() as F?
}