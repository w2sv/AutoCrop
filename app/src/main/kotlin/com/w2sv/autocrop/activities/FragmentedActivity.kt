package com.w2sv.autocrop.activities

import android.annotation.SuppressLint
import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.blogspot.atifsoftwares.animatoolib.R
import com.w2sv.androidutils.ActivityRetriever

abstract class FragmentedActivity : BlankViewBoundActivity() {

    interface Retriever : ActivityRetriever {

        val fragmentedActivity: FragmentedActivity

        class Implementation(context: Context) : Retriever,
                                                 ActivityRetriever.Implementation(context) {
            override val fragmentedActivity: FragmentedActivity = activity as FragmentedActivity
        }
    }

    private val layoutId: Int get() = binding.root.id

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

    abstract fun getRootFragment(): Fragment

    fun getCurrentFragment(): Fragment? =
        supportFragmentManager.findFragmentById(layoutId)

    @Suppress("UNCHECKED_CAST")
    fun <F : Fragment> getCastCurrentFragment(): F? =
        getCurrentFragment() as F?

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
}