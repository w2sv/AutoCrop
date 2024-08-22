package com.w2sv.autocrop.activities

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.blogspot.atifsoftwares.animatoolib.R
import com.w2sv.autocrop.databinding.BlankBinding
import com.w2sv.autocrop.utils.tagName
import com.w2sv.viewboundcontroller.ViewBoundActivity

abstract class ViewBoundFragmentActivity : ViewBoundActivity<BlankBinding>(BlankBinding::class.java) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState == null) {
            launchRootFragment()
        }
    }

    // ==========================
    // Fragment Transactions
    // ==========================

    @SuppressLint("CommitTransaction")
    private fun launchRootFragment() {
        supportFragmentManager
            .beginTransaction()
            .setReorderingAllowed(true)
            .apply {
                val rootFragment = getRootFragment()
                add(
                    binding.root.id,
                    rootFragment,
                    rootFragment.tagName
                )
            }
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
            .replace(binding.root.id, fragment)

    // ======================
    // Fragment Retrieval
    // ======================

    abstract fun getRootFragment(): Fragment

    fun getCurrentFragment(): Fragment? =
        supportFragmentManager.findFragmentById(binding.root.id)
}